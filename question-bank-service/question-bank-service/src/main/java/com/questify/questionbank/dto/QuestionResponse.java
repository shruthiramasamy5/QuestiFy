package com.questify.questionbank.dto;

import com.questify.questionbank.entity.BloomLevel;
import com.questify.questionbank.entity.Difficulty;
import com.questify.questionbank.entity.QuestionType;
import java.time.Instant;
import java.util.List;

public record QuestionResponse(
        Long id,
        String questionText,
        String subject,
        String unit,
        Difficulty difficulty,
        QuestionType questionType,
        Integer marks,
        BloomLevel bloomLevel,
        List<CourseOutcomeResponse> courseOutcomes,
        String createdBy,
        Instant createdAt,
        Instant updatedAt) {
}
