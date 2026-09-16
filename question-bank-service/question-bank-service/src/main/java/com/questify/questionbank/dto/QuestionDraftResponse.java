package com.questify.questionbank.dto;

import com.questify.questionbank.entity.BloomLevel;
import com.questify.questionbank.entity.DraftStatus;
import com.questify.questionbank.entity.Difficulty;
import java.time.Instant;

public record QuestionDraftResponse(
        Long id,
        String questionText,
        String subject,
        String unit,
        Difficulty difficulty,
        Integer marks,
        BloomLevel bloomLevel,
        Long courseOutcomeId,
        DraftStatus status,
        Double confidence,
        String aiModel,
        String createdBy,
        Instant createdAt,
        String reviewedBy,
        Instant reviewedAt,
        Long approvedQuestionId) {
}
