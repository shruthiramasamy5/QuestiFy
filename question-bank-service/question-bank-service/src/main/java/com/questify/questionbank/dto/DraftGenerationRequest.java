package com.questify.questionbank.dto;

import com.questify.questionbank.entity.BloomLevel;
import com.questify.questionbank.entity.Difficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DraftGenerationRequest(
        @NotBlank @Size(max = 128) String subject,
        @NotBlank @Size(max = 64) String unit,
        Long courseOutcomeId,
        @NotNull BloomLevel bloomLevel,
        @NotNull Difficulty difficulty,
        @NotNull @Min(1) @Max(100) Integer marks,
        @NotNull @Min(1) @Max(10) Integer count,
        String topicHint) {
}
