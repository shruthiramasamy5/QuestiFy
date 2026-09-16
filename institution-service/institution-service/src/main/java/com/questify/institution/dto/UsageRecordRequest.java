package com.questify.institution.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Payload used by sibling services (via Eureka discovery) to report usage
 * increments for the current billing period.
 */
public record UsageRecordRequest(
        @NotNull Long institutionId,
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "period must be in yyyy-MM format") String period,
        @PositiveOrZero Integer papersGenerated,
        @PositiveOrZero Integer questionsCreated,
        @PositiveOrZero Integer activeUsers,
        @PositiveOrZero Integer storageUsedMb) {
}
