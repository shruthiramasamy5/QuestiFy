package com.questify.analytics.dto;

import java.util.List;

public record QuestionStats(long totalQuestions,
                            List<CoverageSlice> coCoverage,
                            List<CoverageSlice> bloomCoverage) {

    public static QuestionStats empty() {
        return new QuestionStats(0, List.of(), List.of());
    }
}
