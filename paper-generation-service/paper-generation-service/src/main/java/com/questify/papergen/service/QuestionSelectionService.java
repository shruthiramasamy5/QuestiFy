package com.questify.papergen.service;

import com.questify.papergen.domain.GeneratedQuestion;
import com.questify.papergen.dto.GeneratePaperRequest;
import com.questify.papergen.dto.QuestionDto;
import com.questify.papergen.exception.GenerationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Deterministic, rule based question selection.
 * Rules are applied in order: CO distribution, Bloom distribution,
 * difficulty mix, then remaining slots are filled from the pool.
 */
@Service
public class QuestionSelectionService {

    public List<GeneratedQuestion> select(GeneratePaperRequest request, List<QuestionDto> pool) {
        if (pool == null || pool.isEmpty()) {
            throw new GenerationException("No candidate questions available for subject " + request.getSubjectCode());
        }

        List<QuestionDto> available = new ArrayList<>(pool);
        available.sort(Comparator.comparing(q -> Objects.toString(q.getId(), "")));

        Set<QuestionDto> chosen = new LinkedHashSet<>();

        takeByAttribute(available, chosen, request.getCoDistribution(), QuestionDto::getCourseOutcome);
        takeByAttribute(available, chosen, request.getBloomDistribution(), QuestionDto::getBloomLevel);
        takeByAttribute(available, chosen, request.getDifficultyMix(), QuestionDto::getDifficulty);

        int target = request.getQuestionCount() > 0 ? request.getQuestionCount() : available.size();
        for (QuestionDto q : available) {
            if (chosen.size() >= target) {
                break;
            }
            chosen.add(q);
        }

        List<QuestionDto> chosenList = new ArrayList<>(chosen);
        while (chosenList.size() < target && !available.isEmpty()) {
            for (QuestionDto q : available) {
                if (chosenList.size() >= target) break;
                chosenList.add(q);
            }
            if (chosenList.isEmpty()) break;
        }

        return toGeneratedQuestions(chosenList, Math.min(target, chosenList.size()));
    }

    /**
     * Converts an already-chosen, already-ordered list of questions into
     * numbered {@link GeneratedQuestion}s, truncating to {@code target}.
     * Shared by the rule-based {@link #select} path and by
     * {@code AnthropicAiPaperGenerationProvider}'s AI-assisted path, so
     * both produce identically-shaped results downstream.
     */
    public List<GeneratedQuestion> toGeneratedQuestions(List<QuestionDto> orderedChoice, int target) {
        if (orderedChoice == null || orderedChoice.isEmpty()) {
            throw new GenerationException("No candidate questions available to generate paper.");
        }
        int effectiveTarget = target > 0 ? Math.min(target, orderedChoice.size()) : orderedChoice.size();
        List<GeneratedQuestion> result = new ArrayList<>();
        int seq = 1;
        for (QuestionDto q : orderedChoice) {
            if (seq > effectiveTarget) {
                break;
            }
            result.add(new GeneratedQuestion(q.getId(), q.getText(), q.getCourseOutcome(),
                    q.getBloomLevel(), q.getDifficulty(), q.getMarks(), seq++));
        }
        return result;
    }

    public Map<String, Integer> tally(List<GeneratedQuestion> questions,
                                      java.util.function.Function<GeneratedQuestion, String> attribute) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (GeneratedQuestion q : questions) {
            String key = attribute.apply(q);
            if (key != null) {
                counts.merge(key, 1, Integer::sum);
            }
        }
        return counts;
    }

    private void takeByAttribute(List<QuestionDto> available,
                                 Set<QuestionDto> chosen,
                                 Map<String, Integer> distribution,
                                 java.util.function.Function<QuestionDto, String> attribute) {
        if (distribution == null || distribution.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            int needed = entry.getValue() == null ? 0 : entry.getValue();
            for (QuestionDto q : available) {
                if (needed <= 0) {
                    break;
                }
                if (chosen.contains(q)) {
                    continue;
                }
                if (entry.getKey().equalsIgnoreCase(attribute.apply(q))) {
                    chosen.add(q);
                    needed--;
                }
            }
        }
    }
}
