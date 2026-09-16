package com.questify.questionbank.dto;

import java.util.List;

public record SyllabusUploadResponse(
        String subject,
        int unitsCount,
        int starterDraftsGenerated,
        List<String> units,
        List<String> courseOutcomes
) {
}
