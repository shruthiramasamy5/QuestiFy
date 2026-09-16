package com.questify.papers.dto;

import com.questify.papers.entity.EvaluationScheme;

import java.time.Instant;
import java.util.List;

public record EvaluationSchemeResponse(
        Long id,
        Long paperId,
        Integer totalMarks,
        Integer passingMarks,
        boolean negativeMarking,
        boolean partialMarking,
        String configurationJson,
        List<RubricResponse> rubrics,
        String updatedBy,
        Instant updatedAt
) {
    public record RubricResponse(Long id, String questionId, Integer questionNumber,
                                 Integer marks, String criteria, String stepMarking) { }

    public static EvaluationSchemeResponse from(EvaluationScheme s) {
        List<RubricResponse> rubrics = s.getRubrics().stream()
                .map(r -> new RubricResponse(r.getId(), r.getQuestionId(), r.getQuestionNumber(),
                        r.getMarks(), r.getCriteria(), r.getStepMarking()))
                .toList();
        return new EvaluationSchemeResponse(s.getId(), s.getPaper().getId(), s.getTotalMarks(),
                s.getPassingMarks(), s.isNegativeMarking(), s.isPartialMarking(),
                s.getConfigurationJson(), rubrics, s.getUpdatedBy(), s.getUpdatedAt());
    }
}
