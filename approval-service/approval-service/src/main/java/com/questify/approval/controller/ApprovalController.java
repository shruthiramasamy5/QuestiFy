package com.questify.approval.controller;

import com.questify.approval.domain.ApprovalStatus;
import com.questify.approval.dto.ApprovalRequestResponse;
import com.questify.approval.dto.CreateApprovalRequest;
import com.questify.approval.dto.DecisionRequest;
import com.questify.approval.security.CurrentUser;
import com.questify.approval.service.ApprovalWorkflowService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Backs the frontend routes /approval-queue (status=PENDING) and
 * /approved (status=APPROVED).
 */
@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalWorkflowService workflowService;

    public ApprovalController(ApprovalWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public List<ApprovalRequestResponse> list(@RequestParam(required = false) ApprovalStatus status) {
        return workflowService.queue(CurrentUser.require(), status);
    }

    @GetMapping("/{id}")
    public ApprovalRequestResponse get(@PathVariable Long id) {
        return workflowService.get(CurrentUser.require(), id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD')")
    public ResponseEntity<ApprovalRequestResponse> create(@Valid @RequestBody CreateApprovalRequest payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workflowService.create(CurrentUser.require(), payload));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('HOD','REVIEWER','COURSE_COORDINATOR')")
    public ApprovalRequestResponse approve(@PathVariable Long id,
                                           @RequestBody(required = false) DecisionRequest body) {
        return workflowService.approve(CurrentUser.require(), id, body == null ? null : body.comment());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('HOD','REVIEWER','COURSE_COORDINATOR')")
    public ApprovalRequestResponse reject(@PathVariable Long id,
                                          @RequestBody(required = false) DecisionRequest body) {
        return workflowService.reject(CurrentUser.require(), id, body == null ? null : body.comment());
    }
}
