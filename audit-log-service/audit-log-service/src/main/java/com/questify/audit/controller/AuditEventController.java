package com.questify.audit.controller;

import com.questify.audit.dto.AuditEventQuery;
import com.questify.audit.dto.AuditEventResponse;
import com.questify.audit.dto.CreateAuditEventRequest;
import com.questify.audit.security.CurrentUser;
import com.questify.audit.service.AuditEventService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Backs the frontend routes /audit-log and /activity. */
@RestController
@RequestMapping("/api/audit-events")
public class AuditEventController {

    private final AuditEventService auditEventService;

    public AuditEventController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    /** Any authenticated QuestiFy service or user may append an event. */
    @PostMapping
    public ResponseEntity<AuditEventResponse> append(@Valid @RequestBody CreateAuditEventRequest payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(auditEventService.append(CurrentUser.require(), payload));
    }

    /** Reading the log is restricted to Super Admin and Institution Admin. */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTION_ADMIN')")
    public List<AuditEventResponse> search(@RequestParam(required = false) String actor,
                                           @RequestParam(required = false) String action,
                                           @RequestParam(required = false) Instant from,
                                           @RequestParam(required = false) Instant to,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        AuditEventQuery query = new AuditEventQuery(actor, action, from, to, page, size);
        return auditEventService.search(CurrentUser.require(), query);
    }
}
