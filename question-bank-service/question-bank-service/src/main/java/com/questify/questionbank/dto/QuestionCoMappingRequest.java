package com.questify.questionbank.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record QuestionCoMappingRequest(
        @NotNull Long questionId,
        @NotEmpty Set<Long> courseOutcomeIds) {
}
