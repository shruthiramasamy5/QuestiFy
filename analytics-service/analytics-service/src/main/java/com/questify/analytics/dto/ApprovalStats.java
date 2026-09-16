package com.questify.analytics.dto;

public record ApprovalStats(long pending, long approved, long rejected,
                            Double averageTurnaroundHours) {

    public static ApprovalStats empty() {
        return new ApprovalStats(0, 0, 0, null);
    }
}
