package com.questify.analytics.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

/** Subset of the papers-service / paper-generation-service representation. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RemotePaper(Long id, String title, String status, String createdBy, Instant createdAt) {
}
