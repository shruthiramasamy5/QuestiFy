package com.questify.institution.dto;

import com.questify.institution.domain.InstitutionStatus;

import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull InstitutionStatus status) {
}
