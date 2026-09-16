package com.questify.mlhealth.dto;

import java.time.Instant;

/** Outcome of a single probe against the external ML endpoint. */
public record MlProbeResult(boolean success, Integer httpStatus, long latencyMs, Instant checkedAt,
                            String error) {

    public static MlProbeResult ok(int httpStatus, long latencyMs, Instant checkedAt) {
        return new MlProbeResult(true, httpStatus, latencyMs, checkedAt, null);
    }

    public static MlProbeResult failure(Integer httpStatus, long latencyMs, Instant checkedAt, String error) {
        return new MlProbeResult(false, httpStatus, latencyMs, checkedAt, error);
    }
}
