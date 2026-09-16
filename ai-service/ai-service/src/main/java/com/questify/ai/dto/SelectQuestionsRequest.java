package com.questify.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Map;

/**
 * Asks the model to choose the best subset of {@code candidates} that
 * satisfies the requested marks/CO/Bloom distribution - a re-ranking step
 * on top of paper-generation-service's own rule-based filtering, not a
 * replacement for its guarantees (the caller re-validates the result).
 */
public record SelectQuestionsRequest(
        @NotNull @Positive Integer questionCount,
        @NotNull @Positive Integer totalMarks,
        Map<String, Integer> coDistribution,
        Map<String, Integer> bloomDistribution,
        Map<String, Integer> difficultyMix,
        @NotEmpty @Valid List<CandidateQuestionDto> candidates) {
}
