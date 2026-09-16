package com.questify.questionbank.dto.ai;

public record AiQuestionDraft(String text, String bloomLevel, String difficulty, Integer marks, Double confidence) {
}
