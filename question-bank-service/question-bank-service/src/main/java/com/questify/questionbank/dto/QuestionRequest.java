package com.questify.questionbank.dto;

import com.questify.questionbank.entity.BloomLevel;
import com.questify.questionbank.entity.Difficulty;
import com.questify.questionbank.entity.QuestionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record QuestionRequest(

        @NotNull
        Long institutionId,

        @NotNull
        Long subjectId,

        @NotBlank
        @Size(max = 4000)
        String questionText,

        @NotBlank
        @Size(max = 128)
        String subject,

        @NotBlank
        @Size(max = 64)
        String unit,

        @NotNull
        Difficulty difficulty,

        @NotNull
        QuestionType questionType,

        @NotNull
        @Min(1)
        @Max(100)
        Integer marks,

        @NotNull
        BloomLevel bloomLevel,

        Set<Long> courseOutcomeIds
) {
}