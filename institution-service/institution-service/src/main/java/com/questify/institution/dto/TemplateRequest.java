package com.questify.institution.dto;

import com.questify.institution.domain.TemplateType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TemplateRequest(
        Long institutionId,
        Long departmentId,
        @NotBlank @Size(max = 150) String name,
        @NotNull TemplateType type,
        @Size(max = 500) String description,
        String headerHtml,
        String footerHtml,
        String instructions,
        String layoutJson,
        Boolean defaultTemplate,
        Boolean active) {
}
