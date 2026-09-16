package com.questify.analytics.dto;

import java.util.List;

public record PaperStats(long totalPapers, long generatedPapers, List<MonthlyCount> papersPerMonth) {

    public static PaperStats empty() {
        return new PaperStats(0, 0, List.of());
    }
}
