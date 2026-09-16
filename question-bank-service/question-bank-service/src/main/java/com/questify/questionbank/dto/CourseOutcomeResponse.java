package com.questify.questionbank.dto;

import com.questify.questionbank.entity.BloomLevel;
import java.time.Instant;

public record CourseOutcomeResponse(
        Long id,
        String code,
        String description,
        String subject,
        BloomLevel bloomLevel,
        String createdBy,
        Instant createdAt,
        Instant updatedAt) {
}
