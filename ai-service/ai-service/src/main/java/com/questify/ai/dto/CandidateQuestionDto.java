package com.questify.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** One candidate question offered to the model for selection/ranking. */
public record CandidateQuestionDto(
        @NotBlank String id,
        @NotBlank String text,
        String courseOutcome,
        String bloomLevel,
        String difficulty,
        @NotNull Integer marks) {
}
