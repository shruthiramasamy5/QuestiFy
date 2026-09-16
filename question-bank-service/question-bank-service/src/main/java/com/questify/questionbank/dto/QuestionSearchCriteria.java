package com.questify.questionbank.dto;

import com.questify.questionbank.entity.BloomLevel;
import com.questify.questionbank.entity.Difficulty;
import com.questify.questionbank.entity.QuestionType;

/**
 * Filter criteria for the question search API consumed by the UI and later by
 * paper-generation-service.
 */
public record QuestionSearchCriteria(
        String subject,
        String unit,
        Long courseOutcomeId,
        String courseOutcomeCode,
        BloomLevel bloomLevel,
        Difficulty difficulty,
        QuestionType questionType,
        Integer marks,
        String search) {
}
