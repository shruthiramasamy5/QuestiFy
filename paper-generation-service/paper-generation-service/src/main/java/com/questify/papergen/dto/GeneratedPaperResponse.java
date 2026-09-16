package com.questify.papergen.dto;

import com.questify.papergen.domain.DraftStatus;
import com.questify.papergen.domain.GeneratedPaper;
import com.questify.papergen.domain.GeneratedQuestion;
import com.questify.papergen.domain.GenerationMetadata;

import java.time.Instant;
import java.util.List;

public record GeneratedPaperResponse(
        String id,
        String subjectCode,
        String subjectName,
        String examType,
        Integer totalMarks,
        Integer durationMinutes,
        Integer questionCount,
        DraftStatus status,
        List<GeneratedQuestion> questions,
        GenerationMetadata metadata,
        String createdBy,
        Instant createdAt
) {
    public static GeneratedPaperResponse from(GeneratedPaper p) {
        return new GeneratedPaperResponse(
                p.getId(), p.getSubjectCode(), p.getSubjectName(), p.getExamType(),
                p.getTotalMarks(), p.getDurationMinutes(), p.getQuestionCount(),
                p.getStatus(), p.getQuestions(), p.getMetadata(), p.getCreatedBy(), p.getCreatedAt());
    }
}
