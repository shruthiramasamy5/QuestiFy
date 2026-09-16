package com.questify.institution.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.questify.institution.domain.InstitutionStatus;
import com.questify.institution.domain.PlanTier;

/** Aggregated plan/usage view backing the /plan and /billing screens. */
public record PlanUsageStatsResponse(
        Long institutionId,
        String institutionName,
        PlanTier planTier,
        InstitutionStatus status,
        BigDecimal monthlyPrice,
        LocalDate subscriptionStartDate,
        LocalDate subscriptionEndDate,
        String currentPeriod,
        UsageMetric papers,
        UsageMetric users,
        UsageMetric storageMb,
        int questionsCreatedThisPeriod,
        int departmentCount,
        int templateCount,
        List<PlanUsageResponse> history) {

    public record UsageMetric(int used, int limit, double utilizationPercent) {

        public static UsageMetric of(int used, Integer limit) {
            int effectiveLimit = limit == null ? 0 : limit;
            double percent = effectiveLimit <= 0 ? 0d
                    : Math.round((used * 10000d) / effectiveLimit) / 100d;
            return new UsageMetric(used, effectiveLimit, percent);
        }
    }
}
