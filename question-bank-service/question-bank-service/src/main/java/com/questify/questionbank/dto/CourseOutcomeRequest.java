package com.questify.questionbank.dto;

import com.questify.questionbank.entity.BloomLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CourseOutcomeRequest(
        @NotBlank @Pattern(regexp = "^CO\\d{1,2}$", message = "code must look like CO1") String code,
        @NotBlank @Size(max = 512) String description,
        @NotBlank @Size(max = 128) String subject,
        BloomLevel bloomLevel) {
}
