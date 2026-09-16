package com.questify.ai.dto;

public record QuestionDraftDto(String text, String bloomLevel, String difficulty, Integer marks, Double confidence) {
}
