package com.questify.questionbank.dto;

import java.util.List;

public record SyllabusUploadRequest(
        String subject,
        String subjectCode,
        Long subjectId,
        Long institutionId,
        String syllabusText,
        List<String> extractedUnits
) {
}
