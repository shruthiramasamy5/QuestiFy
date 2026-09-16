package com.questify.institution.dto;

import java.time.Instant;

import com.questify.institution.domain.Template;
import com.questify.institution.domain.TemplateType;

public record TemplateResponse(
        Long id,
        Long institutionId,
        Long departmentId,
        String name,
        TemplateType type,
        String description,
        String headerHtml,
        String footerHtml,
        String instructions,
        String layoutJson,
        boolean defaultTemplate,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {

    public static TemplateResponse from(Template entity) {
        return new TemplateResponse(
                entity.getId(),
                entity.getInstitutionId(),
                entity.getDepartmentId(),
                entity.getName(),
                entity.getType(),
                entity.getDescription(),
                entity.getHeaderHtml(),
                entity.getFooterHtml(),
                entity.getInstructions(),
                entity.getLayoutJson(),
                entity.isDefaultTemplate(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
