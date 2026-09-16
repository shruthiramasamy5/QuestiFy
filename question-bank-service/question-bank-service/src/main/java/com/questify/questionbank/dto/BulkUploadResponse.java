package com.questify.questionbank.dto;

import java.util.List;

public record BulkUploadResponse(
        int totalReceived,
        int imported,
        int skipped,
        List<String> errors
) {
}
