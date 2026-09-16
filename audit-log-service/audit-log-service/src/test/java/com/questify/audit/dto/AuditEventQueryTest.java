package com.questify.audit.dto;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuditEventQueryTest {

    @Test
    void rejectsNegativePage() {
        assertThatThrownBy(() -> new AuditEventQuery(null, null, null, null, -1, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsOversizedPage() {
        assertThatThrownBy(() -> new AuditEventQuery(null, null, null, null, 0, 500))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvertedDateRange() {
        assertThatThrownBy(() -> new AuditEventQuery(null, null,
                Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-05-01T00:00:00Z"), 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
