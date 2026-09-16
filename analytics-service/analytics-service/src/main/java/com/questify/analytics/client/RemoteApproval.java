package com.questify.analytics.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

/** Subset of the approval-service representation. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteApproval(Long id, Long paperId, String status, String currentStage,
                             String createdBy, Instant createdAt, Instant updatedAt) {
}
