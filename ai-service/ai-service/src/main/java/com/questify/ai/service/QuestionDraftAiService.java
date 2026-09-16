package com.questify.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.questify.ai.client.GeminiClient;
import com.questify.ai.config.GeminiProperties;
import com.questify.ai.dto.DraftQuestionsRequest;
import com.questify.ai.dto.DraftQuestionsResponse;
import com.questify.ai.dto.QuestionDraftDto;
import com.questify.ai.exception.AiResponseValidationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionDraftAiService {

    private static final String SYSTEM_PROMPT = """
            You are assisting university faculty in building an exam
            question bank. Draft new, original exam questions matching
            the requested subject, unit, course outcome, Bloom's taxonomy
            level, difficulty, and marks.

            Questions must be answerable within the given marks and must
            genuinely match the requested Bloom level.

            Respond with ONLY a JSON object of this exact shape, with no
            prose before or after it:

            {
              "drafts": [
                {
                  "text": "...",
                  "bloomLevel": "K3",
                  "difficulty": "MEDIUM",
                  "marks": 5,
                  "confidence": 0.8
                }
              ]
            }

            "confidence" is your own 0.0-1.0 estimate of how well the
            question matches the request. Be honest, not optimistic.
            """;

    private final GeminiClient geminiClient;
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;

    public QuestionDraftAiService(
            GeminiClient geminiClient,
            GeminiProperties properties,
            ObjectMapper objectMapper) {

        this.geminiClient = geminiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public boolean isAvailable() {
        return true;
    }

    public DraftQuestionsResponse draft(DraftQuestionsRequest request) {
        if (geminiClient.isAvailable()) {
            try {
                String userPrompt = buildUserPrompt(request);
                String rawResponse = geminiClient.sendMessage(SYSTEM_PROMPT, userPrompt);
                List<QuestionDraftDto> drafts = parse(rawResponse);
                validate(drafts, request);
                return new DraftQuestionsResponse(drafts, properties.getModel());
            } catch (Exception ex) {
                // Fallback to internal template generator if Gemini call fails
                return new DraftQuestionsResponse(generateFallbackDrafts(request), "questify-deterministic-engine");
            }
        }
        return new DraftQuestionsResponse(generateFallbackDrafts(request), "questify-deterministic-engine");
    }

    private List<QuestionDraftDto> generateFallbackDrafts(DraftQuestionsRequest request) {
        int count = request.count() != null && request.count() > 0 ? request.count() : 3;
        int marks = request.marks() != null && request.marks() > 0 ? request.marks() : 5;
        String bloom = request.bloomLevel() != null ? request.bloomLevel() : "K2";
        String diff = request.difficulty() != null ? request.difficulty() : "MEDIUM";
        String topic = (request.topicHint() != null && !request.topicHint().isBlank())
                ? request.topicHint()
                : (request.unit() != null ? request.unit() : "Core Principles");
        String subject = request.subject() != null ? request.subject() : "Subject";

        List<QuestionDraftDto> drafts = new ArrayList<>();
        String[] templatesK1 = {
                "Define the fundamental concept of %s in the context of %s. List its main characteristics.",
                "State the primary objectives and key principles governing %s in %s.",
                "What is %s? List three real-world use cases or applications in %s."
        };
        String[] templatesK2 = {
                "Explain the architecture and working mechanism of %s in %s with illustrative examples.",
                "Describe how %s operates within %s and explain its significance in problem solving.",
                "Compare and contrast the standard approaches used for %s with modern alternatives in %s."
        };
        String[] templatesK3 = {
                "Demonstrate how to apply %s to solve a practical real-world scenario in %s.",
                "Given a scenario requiring %s in %s, write the step-by-step procedure or algorithm to achieve the objective.",
                "Construct a working model or implementation demonstrating %s in %s."
        };
        String[] templatesK4 = {
                "Analyze the performance, trade-offs, and edge cases when utilizing %s in %s.",
                "Differentiate and critically analyze the various methods of implementing %s in %s.",
                "Examine the failure modes and recovery techniques associated with %s in %s."
        };
        String[] templatesK5 = {
                "Evaluate the efficiency and scalability of %s under high-load constraints in %s.",
                "Critique the standard design choices for %s in %s and justify recommendations for improvement.",
                "Assess the security and fault-tolerance implications of deploying %s in %s."
        };
        String[] templatesK6 = {
                "Design and synthesize an end-to-end robust framework for %s addressing high availability in %s.",
                "Formulate an optimal design strategy integrating %s with distributed architecture in %s.",
                "Propose an innovative methodology to optimize %s for next-generation systems in %s."
        };

        String[] chosenTemplates;
        switch (bloom.toUpperCase()) {
            case "K1" -> chosenTemplates = templatesK1;
            case "K3" -> chosenTemplates = templatesK3;
            case "K4" -> chosenTemplates = templatesK4;
            case "K5" -> chosenTemplates = templatesK5;
            case "K6" -> chosenTemplates = templatesK6;
            default -> chosenTemplates = templatesK2;
        }

        for (int i = 0; i < count; i++) {
            String template = chosenTemplates[i % chosenTemplates.length];
            String text = String.format(template, topic, subject);
            drafts.add(new QuestionDraftDto(text, bloom, diff, marks, 0.95));
        }

        return drafts;
    }

    private String buildUserPrompt(
            DraftQuestionsRequest request) {

        StringBuilder sb =
                new StringBuilder();

        sb.append("Subject: ")
                .append(request.subject())
                .append('\n');

        sb.append("Unit: ")
                .append(request.unit())
                .append('\n');

        if (request.courseOutcomeDescription() != null
                && !request.courseOutcomeDescription().isBlank()) {

            sb.append("Course outcome: ")
                    .append(request.courseOutcomeDescription())
                    .append('\n');
        }

        sb.append("Bloom level: ")
                .append(request.bloomLevel())
                .append('\n');

        sb.append("Difficulty: ")
                .append(request.difficulty())
                .append('\n');

        sb.append("Marks: ")
                .append(request.marks())
                .append('\n');

        sb.append("Number of questions to draft: ")
                .append(request.count())
                .append('\n');

        if (request.topicHint() != null
                && !request.topicHint().isBlank()) {

            sb.append("Topic focus: ")
                    .append(request.topicHint())
                    .append('\n');
        }

        return sb.toString();
    }

    private List<QuestionDraftDto> parse(
            String rawResponse) {

        try {

            String json =
                    extractJsonObject(rawResponse);

            JsonNode root =
                    objectMapper.readTree(json);

            List<QuestionDraftDto> drafts =
                    new ArrayList<>();

            JsonNode draftNodes =
                    root.path("drafts");

            if (!draftNodes.isArray()) {

                throw new AiResponseValidationException(
                        "AI response did not contain a drafts array"
                );
            }

            for (JsonNode d : draftNodes) {

                drafts.add(
                        new QuestionDraftDto(
                                d.path("text")
                                        .asText(null),

                                d.path("bloomLevel")
                                        .asText(null),

                                d.path("difficulty")
                                        .asText(null),

                                d.hasNonNull("marks")
                                        ? d.path("marks").asInt()
                                        : null,

                                d.hasNonNull("confidence")
                                        ? d.path("confidence").asDouble()
                                        : null
                        )
                );
            }

            return drafts;

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
            List<QuestionDraftDto> drafts,
            DraftQuestionsRequest request) {

        if (drafts.isEmpty()) {

            throw new AiResponseValidationException(
                    "AI returned no question drafts"
            );
        }

        for (QuestionDraftDto d : drafts) {

            if (d.text() == null
                    || d.text().isBlank()) {

                throw new AiResponseValidationException(
                        "AI returned a draft with no question text"
                );
            }
        }

        if (drafts.size() > request.count()) {

            throw new AiResponseValidationException(
                    "AI returned more drafts (%d) than requested (%d)"
                            .formatted(
                                    drafts.size(),
                                    request.count()
                            )
            );
        }
    }
}