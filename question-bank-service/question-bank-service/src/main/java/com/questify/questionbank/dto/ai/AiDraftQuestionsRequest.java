package com.questify.questionbank.dto.ai;

public record AiDraftQuestionsRequest(String subject, String unit, String courseOutcomeDescription,
                                       String bloomLevel, String difficulty, Integer marks, Integer count,
                                       String topicHint) {
}
