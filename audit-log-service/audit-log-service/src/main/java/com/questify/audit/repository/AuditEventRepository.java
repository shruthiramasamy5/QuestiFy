package com.questify.audit.repository;

import com.questify.audit.domain.AuditEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Append-only store. Deliberately exposes no delete/update helpers beyond the
 * inherited ones, which are never called by application code.
 */
@Repository
public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
}
