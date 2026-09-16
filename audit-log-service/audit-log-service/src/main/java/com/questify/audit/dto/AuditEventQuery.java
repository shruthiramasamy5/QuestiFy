package com.questify.audit.dto;

import java.time.Instant;

/** Filters supported by {@code GET /api/audit-events}. */
public record AuditEventQuery(String actor, String action, Instant from, Instant to,
                              int page, int size) {

    public AuditEventQuery {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size <= 0 || size > 200) {
            throw new IllegalArgumentException("size must be between 1 and 200");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must not be after to");
        }
    }
}
