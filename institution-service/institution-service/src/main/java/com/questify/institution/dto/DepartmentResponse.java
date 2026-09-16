package com.questify.institution.dto;

import java.time.Instant;

import com.questify.institution.domain.Department;

public record DepartmentResponse(
        Long id,
        Long institutionId,
        String name,
        String code,
        String headName,
        String headEmail,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {

    public static DepartmentResponse from(Department entity) {
        return new DepartmentResponse(
                entity.getId(),
                entity.getInstitutionId(),
                entity.getName(),
                entity.getCode(),
                entity.getHeadName(),
                entity.getHeadEmail(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
