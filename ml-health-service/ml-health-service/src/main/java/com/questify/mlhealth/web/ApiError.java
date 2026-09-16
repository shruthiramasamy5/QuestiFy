package com.questify.mlhealth.web;

import java.time.Instant;

/** Uniform error payload returned by every endpoint. */
public record ApiError(Instant timestamp, int status, String error, String message) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(Instant.now(), status, error, message);
    }
}
