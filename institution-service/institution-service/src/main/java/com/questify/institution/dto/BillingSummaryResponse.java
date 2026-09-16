package com.questify.institution.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.questify.institution.domain.InstitutionStatus;
import com.questify.institution.domain.PlanTier;

/** One row of the super-admin billing table. */
public record BillingSummaryResponse(
        Long institutionId,
        String institutionName,
        String institutionCode,
        PlanTier planTier,
        InstitutionStatus status,
        BigDecimal monthlyPrice,
        LocalDate subscriptionStartDate,
        LocalDate subscriptionEndDate,
        String currentPeriod,
        int papersGenerated,
        int activeUsers,
        int storageUsedMb) {
}
