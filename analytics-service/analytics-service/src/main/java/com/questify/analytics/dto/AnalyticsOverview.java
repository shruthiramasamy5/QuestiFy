package com.questify.analytics.dto;

import java.time.Instant;
import java.util.List;

/**
 * Role-scoped analytics payload for the /analytics frontend route.
 *
 * @param scope FACULTY (own data) or DEPARTMENT (HOD, whole department)
 */
public record AnalyticsOverview(String scope,
                                Instant generatedAt,
                                QuestionStats questions,
                                PaperStats papers,
                                ApprovalStats approvals,
                                List<String> degradedSources) {
}
