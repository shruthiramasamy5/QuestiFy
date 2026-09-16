package com.questify.audit.service;

import com.questify.audit.domain.AuditEvent;
import com.questify.audit.dto.AuditEventQuery;
import com.questify.audit.dto.AuditEventResponse;
import com.questify.audit.dto.CreateAuditEventRequest;
import com.questify.audit.repository.AuditEventRepository;
import com.questify.audit.security.AuthenticatedUser;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * Append-only audit log. Records can be created and read; there is no update or
 * delete path anywhere in the service, so historical events cannot be altered
 * through the API.
 */
@Service
public class AuditEventService {

    private final AuditEventRepository repository;
    private final MongoTemplate mongoTemplate;

    public AuditEventService(AuditEventRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    public AuditEventResponse append(AuthenticatedUser caller, CreateAuditEventRequest payload) {
        AuditEvent event = new AuditEvent();
        event.setActorUserId(payload.actorUserId() == null || payload.actorUserId().isBlank()
                ? caller.userId() : payload.actorUserId());
        event.setActorRole(payload.actorRole() == null || payload.actorRole().isBlank()
                ? caller.roles().stream().sorted().findFirst().orElse("UNKNOWN")
                : payload.actorRole().toUpperCase());
        event.setAction(payload.action().trim().toUpperCase());
        event.setTargetEntityType(payload.targetEntityType());
        event.setTargetEntityId(payload.targetEntityId());
        event.setInstitutionId(caller.institutionId());
        event.setTimestamp(payload.timestamp() == null ? Instant.now() : payload.timestamp());
        event.setMetadata(payload.metadata() == null ? Map.of() : payload.metadata());
        return AuditEventResponse.from(repository.save(event));
    }

    public List<AuditEventResponse> search(AuthenticatedUser caller, AuditEventQuery filters) {
        Query query = new Query();
        if (filters.actor() != null && !filters.actor().isBlank()) {
            query.addCriteria(Criteria.where("actorUserId").is(filters.actor().trim()));
        }
        if (filters.action() != null && !filters.action().isBlank()) {
            query.addCriteria(Criteria.where("action").is(filters.action().trim().toUpperCase()));
        }
        if (filters.from() != null && filters.to() != null) {
            query.addCriteria(Criteria.where("timestamp").gte(filters.from()).lte(filters.to()));
        } else if (filters.from() != null) {
            query.addCriteria(Criteria.where("timestamp").gte(filters.from()));
        } else if (filters.to() != null) {
            query.addCriteria(Criteria.where("timestamp").lte(filters.to()));
        }
        // Institution Admins only see their own tenant; Super Admins see everything.
        if (!caller.hasRole("SUPER_ADMIN") && caller.institutionId() != null) {
            query.addCriteria(Criteria.where("institutionId").is(caller.institutionId()));
        }
        query.with(PageRequest.of(filters.page(), filters.size(),
                Sort.by(Sort.Direction.DESC, "timestamp")));
        return mongoTemplate.find(query, AuditEvent.class).stream()
                .map(AuditEventResponse::from)
                .toList();
    }
}
