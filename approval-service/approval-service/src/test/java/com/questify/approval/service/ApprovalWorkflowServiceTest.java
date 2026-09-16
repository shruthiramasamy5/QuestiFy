package com.questify.approval.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.questify.approval.domain.ApprovalRequest;
import com.questify.approval.domain.ApprovalStage;
import com.questify.approval.domain.ApprovalStatus;
import com.questify.approval.dto.ApprovalRequestResponse;
import com.questify.approval.dto.CreateApprovalRequest;
import com.questify.approval.repository.ApprovalRequestRepository;
import com.questify.approval.security.AuthenticatedUser;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ApprovalWorkflowServiceTest {

    @Mock
    private ApprovalRequestRepository repository;

    private ApprovalWorkflowService service;

    private final AuthenticatedUser hod = new AuthenticatedUser("u-hod", "hod@questify.test", "inst-1", Set.of("HOD"));
    private final AuthenticatedUser reviewer =
            new AuthenticatedUser("u-rev", "rev@questify.test", "inst-1", Set.of("REVIEWER"));
    private final AuthenticatedUser faculty =
            new AuthenticatedUser("u-fac", "fac@questify.test", "inst-1", Set.of("FACULTY"));

    @BeforeEach
    void setUp() {
        service = new ApprovalWorkflowService(repository);
    }

    private ApprovalRequest pendingAt(ApprovalStage stage) {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        request.setPaperId(42L);
        request.setCreatedBy("u-fac");
        request.setCreatedAt(Instant.now());
        request.setCurrentStage(stage);
        request.setStatus(ApprovalStatus.PENDING);
        return request;
    }

    @Test
    void hodApprovalMovesRequestToReviewerStage() {
        ApprovalRequest request = pendingAt(ApprovalStage.HOD);
        when(repository.findByIdWithSteps(1L)).thenReturn(Optional.of(request));
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovalRequestResponse response = service.approve(hod, 1L, "Looks good");

        assertThat(response.currentStage()).isEqualTo(ApprovalStage.REVIEWER);
        assertThat(response.status()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(response.steps()).hasSize(1);
    }

    @Test
    void reviewerApprovalCompletesWorkflow() {
        ApprovalRequest request = pendingAt(ApprovalStage.REVIEWER);
        when(repository.findByIdWithSteps(1L)).thenReturn(Optional.of(request));
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovalRequestResponse response = service.approve(reviewer, 1L, null);

        assertThat(response.currentStage()).isEqualTo(ApprovalStage.COMPLETED);
        assertThat(response.status()).isEqualTo(ApprovalStatus.APPROVED);
    }

    @Test
    void reviewerCannotActionRequestStillWaitingForHod() {
        when(repository.findByIdWithSteps(1L)).thenReturn(Optional.of(pendingAt(ApprovalStage.HOD)));

        assertThatThrownBy(() -> service.approve(reviewer, 1L, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void facultyCannotApproveAnything() {
        when(repository.findByIdWithSteps(1L)).thenReturn(Optional.of(pendingAt(ApprovalStage.HOD)));

        assertThatThrownBy(() -> service.approve(faculty, 1L, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectionTerminatesWorkflow() {
        ApprovalRequest request = pendingAt(ApprovalStage.HOD);
        when(repository.findByIdWithSteps(1L)).thenReturn(Optional.of(request));
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovalRequestResponse response = service.reject(hod, 1L, "CO coverage insufficient");

        assertThat(response.status()).isEqualTo(ApprovalStatus.REJECTED);
    }

    @Test
    void queueIsFilteredByRoleStage() {
        ApprovalRequest atHod = pendingAt(ApprovalStage.HOD);
        when(repository.findByStatusAndCurrentStageOrderByCreatedAtDesc(ApprovalStatus.PENDING, ApprovalStage.HOD))
                .thenReturn(List.of(atHod));

        List<ApprovalRequestResponse> queue = service.queue(hod, null);

        assertThat(queue).hasSize(1);
        assertThat(queue.get(0).currentStage()).isEqualTo(ApprovalStage.HOD);
    }

    @Test
    void facultySeesOnlyOwnPendingRequests() {
        List<ApprovalRequestResponse> queue = service.queue(faculty, null);
        assertThat(queue).isEmpty();
    }

    @Test
    void duplicatePendingRequestIsRejected() {
        when(repository.findByPaperIdAndStatus(42L, ApprovalStatus.PENDING))
                .thenReturn(Optional.of(pendingAt(ApprovalStage.HOD)));

        assertThatThrownBy(() -> service.create(faculty, new CreateApprovalRequest(42L, "Midterm")))
                .isInstanceOf(IllegalStateException.class);
    }
}
