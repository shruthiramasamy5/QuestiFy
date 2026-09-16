package com.questify.institution.dto;

import com.questify.institution.domain.PlanUsage;

public record PlanUsageResponse(
        Long institutionId,
        String period,
        int papersGenerated,
        int questionsCreated,
        int activeUsers,
        int storageUsedMb) {

    public static PlanUsageResponse from(PlanUsage entity) {
        return new PlanUsageResponse(
                entity.getInstitutionId(),
                entity.getPeriod(),
                entity.getPapersGenerated(),
                entity.getQuestionsCreated(),
                entity.getActiveUsers(),
                entity.getStorageUsedMb());
    }
}
