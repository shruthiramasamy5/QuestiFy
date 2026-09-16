package com.questify.questionbank.dto;

import com.questify.questionbank.entity.BloomLevel;
import com.questify.questionbank.entity.Difficulty;
import com.questify.questionbank.entity.QuestionType;

import java.util.Set;

public record DraftApprovalRequest(

        Long institutionId,

        Long subjectId,

        String questionText,

        Difficulty difficulty,

        QuestionType questionType,

        Integer marks,

        BloomLevel bloomLevel,

        Set<Long> courseOutcomeIds
) {
}