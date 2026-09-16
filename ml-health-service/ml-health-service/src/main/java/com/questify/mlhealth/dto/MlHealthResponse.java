package com.questify.mlhealth.dto;

import java.time.Instant;

/**
 * Payload for the /ml-health frontend route.
 *
 * @param status UP, DOWN or UNKNOWN (never probed yet)
 */
public record MlHealthResponse(String endpoint,
                               String status,
                               Integer lastHttpStatus,
                               Long latencyMs,
                               Double averageLatencyMs,
                               Instant lastCheckedAt,
                               long totalChecks,
                               long errorCount,
                               double errorRate,
                               String lastError) {
}
