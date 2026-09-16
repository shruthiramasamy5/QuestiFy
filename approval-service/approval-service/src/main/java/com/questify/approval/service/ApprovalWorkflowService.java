package com.questify.approval.service;

import com.questify.approval.domain.ApprovalRequest;
import com.questify.approval.domain.ApprovalStage;
import com.questify.approval.domain.ApprovalStatus;
import com.questify.approval.domain.ApprovalStep;
import com.questify.approval.dto.ApprovalRequestResponse;
import com.questify.approval.dto.CreateApprovalRequest;
import com.questify.approval.repository.ApprovalRequestRepository;
import com.questify.approval.security.AuthenticatedUser;
import com.questify.approval.web.NotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the QuestiFy approval workflow: FACULTY submits, HOD approves,
 * then a REVIEWER gives the final approval.
 *
 * <p>Authorization rule: a user may only action a request whose current stage
 * matches one of the user's roles. Everything else is rejected with 403.</p>
 */
@Service
public class ApprovalWorkflowService {

    private final ApprovalRequestRepository requestRepository;

    public ApprovalWorkflowService(ApprovalRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    /** Queue filtered by the caller's role and the current workflow stage. */
    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> queue(AuthenticatedUser user, ApprovalStatus status) {
        ApprovalStatus effectiveStatus = status == null ? ApprovalStatus.PENDING : status;
        List<ApprovalRequest> requests;
        if (effectiveStatus == ApprovalStatus.PENDING) {
            requests = visibleStages(user).stream()
                    .flatMap(stage -> requestRepository
                            .findByStatusAndCurrentStageOrderByCreatedAtDesc(ApprovalStatus.PENDING, stage)
                            .stream())
                    .toList();
        } else {
            requests = requestRepository.findByStatusOrderByCreatedAtDesc(effectiveStatus);
        }
        if (isFacultyOnly(user)) {
            // Faculty only ever see their own submissions.
            requests = requests.stream()
                    .filter(request -> user.userId() != null && user.userId().equals(request.getCreatedBy()))
                    .toList();
        }
        return requests.stream().map(ApprovalRequestResponse::summary).toList();
    }

    @Transactional(readOnly = true)
    public ApprovalRequestResponse get(AuthenticatedUser user, Long id) {
        ApprovalRequest request = requestRepository.findByIdWithSteps(id)
                .orElseThrow(() -> new NotFoundException("Approval request " + id + " not found"));
        if (isFacultyOnly(user) && !user.userId().equals(request.getCreatedBy())) {
            throw new AccessDeniedException("You cannot view this approval request");
        }
        return ApprovalRequestResponse.detail(request);
    }

    @Transactional
    public ApprovalRequestResponse create(AuthenticatedUser user, CreateApprovalRequest payload) {
        requestRepository.findByPaperIdAndStatus(payload.paperId(), ApprovalStatus.PENDING)
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Paper " + payload.paperId() + " already has a pending approval request");
                });
        ApprovalRequest request = new ApprovalRequest();
        request.setPaperId(payload.paperId());
        request.setPaperTitle(payload.paperTitle());
        request.setInstitutionId(user.institutionId());
        request.setCreatedBy(user.userId());
        request.setCurrentStage(ApprovalStage.HOD);
        request.setStatus(ApprovalStatus.PENDING);
        request.addStep(step(ApprovalStage.FACULTY, "FACULTY", user.userId(), ApprovalStatus.APPROVED,
                "Submitted for approval"));
        return ApprovalRequestResponse.detail(requestRepository.save(request));
    }

    @Transactional
    public ApprovalRequestResponse approve(AuthenticatedUser user, Long id, String comment) {
        ApprovalRequest request = loadActionable(user, id);
        ApprovalStage stage = request.getCurrentStage();
        request.addStep(step(stage, stage.approverRole(), user.userId(), ApprovalStatus.APPROVED, comment));
        ApprovalStage next = stage.next();
        request.setCurrentStage(next);
        request.setStatus(next.isTerminal() ? ApprovalStatus.APPROVED : ApprovalStatus.PENDING);
        request.setUpdatedAt(Instant.now());
        return ApprovalRequestResponse.detail(requestRepository.save(request));
    }

    @Transactional
    public ApprovalRequestResponse reject(AuthenticatedUser user, Long id, String comment) {
        ApprovalRequest request = loadActionable(user, id);
        ApprovalStage stage = request.getCurrentStage();
        request.addStep(step(stage, stage.approverRole(), user.userId(), ApprovalStatus.REJECTED, comment));
        request.setStatus(ApprovalStatus.REJECTED);
        request.setUpdatedAt(Instant.now());
        return ApprovalRequestResponse.detail(requestRepository.save(request));
    }

    private ApprovalRequest loadActionable(AuthenticatedUser user, Long id) {
        ApprovalRequest request = requestRepository.findByIdWithSteps(id)
                .orElseThrow(() -> new NotFoundException("Approval request " + id + " not found"));
        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Approval request " + id + " is already " + request.getStatus());
        }
        ApprovalStage stage = request.getCurrentStage();
        String requiredRole = stage.approverRole();
        if (requiredRole == null || !user.hasRole(requiredRole)) {
            throw new AccessDeniedException(
                    "Request " + id + " is at stage " + stage + " and requires the " + requiredRole + " role");
        }
        return request;
    }

    private List<ApprovalStage> visibleStages(AuthenticatedUser user) {
        return List.of(ApprovalStage.HOD, ApprovalStage.REVIEWER).stream()
                .filter(stage -> user.hasRole(stage.approverRole()))
                .toList();
    }

    private boolean isFacultyOnly(AuthenticatedUser user) {
        return !user.hasRole("HOD") && !user.hasRole("REVIEWER");
    }

    private ApprovalStep step(ApprovalStage stage, String role, String actionedBy,
                              ApprovalStatus decision, String comment) {
        ApprovalStep step = new ApprovalStep();
        step.setStage(stage);
        step.setApproverRole(role);
        step.setActionedBy(actionedBy);
        step.setDecision(decision);
        step.setComment(comment);
        step.setTimestamp(Instant.now());
        return step;
    }
}
