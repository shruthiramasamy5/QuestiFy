package com.questify.papers.dto;

import com.questify.papers.entity.Paper;
import com.questify.papers.entity.PaperStatus;

import java.time.Instant;

public record PaperResponse(
        Long id,
        String title,
        String subjectCode,
        String subjectName,
        String examType,
        Integer totalMarks,
        Integer durationMinutes,
        String generatedDraftId,
        PaperStatus status,
        String institutionId,
        String createdBy,
        Instant createdAt,
        Instant updatedAt,
        java.util.List<PaperQuestionDto> questions,
        EvaluationSchemeResponse evaluationScheme
) {
    public static PaperResponse from(Paper p) {
        java.util.List<PaperQuestionDto> qs = java.util.List.of();
        try {
            if (p.getQuestions() != null) {
                qs = p.getQuestions().stream().map(PaperQuestionDto::from).toList();
            }
        } catch (Exception ignored) {}

        EvaluationSchemeResponse eval = null;
        try {
            if (p.getEvaluationScheme() != null) {
                eval = EvaluationSchemeResponse.from(p.getEvaluationScheme());
            }
        } catch (Exception ignored) {}

        return new PaperResponse(
                p.getId(), p.getTitle(), p.getSubjectCode(), p.getSubjectName(),
                p.getExamType(), p.getTotalMarks(), p.getDurationMinutes(), p.getGeneratedDraftId(),
                p.getStatus(), p.getInstitutionId(), p.getCreatedBy(), p.getCreatedAt(), p.getUpdatedAt(),
                qs, eval
        );
    }
}
