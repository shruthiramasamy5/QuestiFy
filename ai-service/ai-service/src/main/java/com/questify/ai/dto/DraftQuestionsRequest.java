package com.questify.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Asks the model to draft new candidate questions for the review queue. */
public record DraftQuestionsRequest(
        @NotBlank String subject,
        @NotBlank String unit,
        String courseOutcomeDescription,
        @NotBlank String bloomLevel,
        @NotBlank String difficulty,
        @NotNull @Positive Integer marks,
        @NotNull @Positive @Max(10) Integer count,
        String topicHint) {
}
