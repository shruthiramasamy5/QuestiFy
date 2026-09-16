package com.questify.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;

/**
 * Payload written by any QuestiFy service. {@code actorUserId} / {@code actorRole}
 * are optional: when omitted they are taken from the caller's JWT.
 */
public record CreateAuditEventRequest(String actorUserId,
                                      String actorRole,
                                      @NotBlank @Size(max = 100) String action,
                                      @Size(max = 100) String targetEntityType,
                                      @Size(max = 100) String targetEntityId,
                                      Instant timestamp,
                                      Map<String, Object> metadata) {
}
