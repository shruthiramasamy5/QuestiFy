package com.questify.papers.dto;

import com.questify.papers.entity.PaperQuestion;

public record PaperQuestionDto(
        Long id,
        Long questionId,
        Integer questionOrder,
        Integer marks,
        String section,
        String questionText,
        String bloomLevel,
        String courseOutcome,
        String unit,
        String difficulty
) {
    public static PaperQuestionDto from(PaperQuestion pq) {
        return new PaperQuestionDto(
                pq.getId(),
                pq.getQuestionId(),
                pq.getQuestionOrder(),
                pq.getMarks(),
                pq.getSection(),
                pq.getQuestionText(),
                pq.getBloomLevel(),
                pq.getCourseOutcome(),
                pq.getUnit(),
                pq.getDifficulty()
        );
    }
}
