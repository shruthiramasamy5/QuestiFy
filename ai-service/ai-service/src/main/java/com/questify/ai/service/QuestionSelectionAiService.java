package com.questify.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.questify.ai.client.GeminiClient;
import com.questify.ai.dto.CandidateQuestionDto;
import com.questify.ai.dto.SelectQuestionsRequest;
import com.questify.ai.dto.SelectQuestionsResponse;
import com.questify.ai.exception.AiResponseValidationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionSelectionAiService {

    private static final String SYSTEM_PROMPT = """
            You are assisting a university exam-paper generation system.
            You will be given a pool of candidate exam questions with id,
            course outcome, Bloom's taxonomy level, difficulty, and marks.

            Choose exactly the requested number of questions from the given
            candidates.

            NEVER invent a question or an id that is not in the candidate list.

            Choose questions that best satisfy:
            - requested total marks
            - CO distribution
            - Bloom distribution
            - difficulty distribution
            - good spread of topics

            Respond with ONLY a JSON object of this exact shape:

            {
              "selectedQuestionIds": ["id1", "id2"],
              "reasoning": "one short sentence"
            }

            No prose before or after the JSON.
            """;

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public QuestionSelectionAiService(
            GeminiClient geminiClient,
            ObjectMapper objectMapper) {

        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public boolean isAvailable() {
        return geminiClient.isAvailable();
    }

    public SelectQuestionsResponse select(
            SelectQuestionsRequest request) {

        String userPrompt =
                buildUserPrompt(request);

        String rawResponse =
                geminiClient.sendMessage(
                        SYSTEM_PROMPT,
                        userPrompt
                );

        SelectQuestionsResponse parsed =
                parse(rawResponse);

        validate(parsed, request);

        return parsed;
    }

    private String buildUserPrompt(
            SelectQuestionsRequest request) {

        StringBuilder sb =
                new StringBuilder();

        sb.append("Requested question count: ")
                .append(request.questionCount())
                .append('\n');

        sb.append("Requested total marks: ")
                .append(request.totalMarks())
                .append('\n');

        if (request.coDistribution() != null
                && !request.coDistribution().isEmpty()) {

            sb.append("Target CO distribution: ")
                    .append(request.coDistribution())
                    .append('\n');
        }

        if (request.bloomDistribution() != null
                && !request.bloomDistribution().isEmpty()) {

            sb.append("Target Bloom distribution: ")
                    .append(request.bloomDistribution())
                    .append('\n');
        }

        if (request.difficultyMix() != null
                && !request.difficultyMix().isEmpty()) {

            sb.append("Target difficulty mix: ")
                    .append(request.difficultyMix())
                    .append('\n');
        }

        sb.append("\nCandidates:\n");

        sb.append(
                request.candidates()
                        .stream()
                        .map(this::candidateLine)
                        .collect(Collectors.joining("\n"))
        );

        return sb.toString();
    }

    private String candidateLine(
            CandidateQuestionDto c) {

        return "id=%s | marks=%s | co=%s | bloom=%s | difficulty=%s | text=%s"
                .formatted(
                        c.id(),
                        c.marks(),
                        c.courseOutcome(),
                        c.bloomLevel(),
                        c.difficulty(),
                        c.text()
                );
    }

    private SelectQuestionsResponse parse(
            String rawResponse) {

        try {

            String json =
                    extractJsonObject(rawResponse);

            JsonNode node =
                    objectMapper.readTree(json);

            JsonNode idsNode =
                    node.path("selectedQuestionIds");

            if (!idsNode.isArray()) {

                throw new AiResponseValidationException(
                        "AI response did not contain selectedQuestionIds array"
                );
            }

            List<String> ids =
                    new ArrayList<>();

            for (JsonNode idNode : idsNode) {

                ids.add(
                        idNode.asText()
                );
            }

            String reasoning =
                    node.path("reasoning")
                            .asText("");

            return new SelectQuestionsResponse(
                    ids,
                    reasoning
            );

        } catch (AiResponseValidationException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new AiResponseValidationException(
                    "AI response was not valid JSON in the expected shape: "
                            + ex.getMessage()
            );
        }
    }

    private String extractJsonObject(
            String raw) {

        if (raw == null || raw.isBlank()) {

            throw new AiResponseValidationException(
                    "AI returned an empty response"
            );
        }

        int start =
                raw.indexOf('{');

        int end =
                raw.lastIndexOf('}');

        if (start < 0 || end < start) {

            throw new AiResponseValidationException(
                    "AI response did not contain a JSON object: "
                            + raw
            );
        }

        return raw.substring(
                start,
                end + 1
        );
    }

    private void validate(
            SelectQuestionsResponse response,
            SelectQuestionsRequest request) {

        if (response.selectedQuestionIds() == null
                || response.selectedQuestionIds().isEmpty()) {

            throw new AiResponseValidationException(
                    "AI returned no selected question ids"
            );
        }

        Set<String> candidateIds =
                request.candidates()
                        .stream()
                        .map(CandidateQuestionDto::id)
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        for (String id :
                response.selectedQuestionIds()) {

            if (!candidateIds.contains(id)) {

                throw new AiResponseValidationException(
                        "AI selected a question id that was not "
                                + "in the candidate pool: "
                                + id
                );
            }
        }

        Set<String> distinct =
                new LinkedHashSet<>(
                        response.selectedQuestionIds()
                );

        if (distinct.size()
                != response.selectedQuestionIds().size()) {

            throw new AiResponseValidationException(
                    "AI selected the same question id more than once"
            );
        }

        if (response.selectedQuestionIds().size()
                != request.questionCount()) {

            throw new AiResponseValidationException(
                    "AI returned %d question ids but %d were requested"
                            .formatted(
                                    response.selectedQuestionIds().size(),
                                    request.questionCount()
                            )
            );
        }
    }
}