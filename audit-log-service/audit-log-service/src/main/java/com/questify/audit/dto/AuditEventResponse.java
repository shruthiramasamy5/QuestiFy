package com.questify.audit.dto;

import com.questify.audit.domain.AuditEvent;
import java.time.Instant;
import java.util.Map;

public record AuditEventResponse(String id, String actorUserId, String actorRole, String action,
                                 String targetEntityType, String targetEntityId, Instant timestamp,
                                 Map<String, Object> metadata) {

    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(event.getId(), event.getActorUserId(), event.getActorRole(),
                event.getAction(), event.getTargetEntityType(), event.getTargetEntityId(),
                event.getTimestamp(), event.getMetadata() == null ? Map.of() : event.getMetadata());
    }
}
