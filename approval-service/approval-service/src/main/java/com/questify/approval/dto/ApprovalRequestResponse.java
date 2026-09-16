package com.questify.approval.dto;

import com.questify.approval.domain.ApprovalRequest;
import com.questify.approval.domain.ApprovalStage;
import com.questify.approval.domain.ApprovalStatus;
import java.time.Instant;
import java.util.List;

public record ApprovalRequestResponse(Long id, Long paperId, String paperTitle, ApprovalStage currentStage,
                                      ApprovalStatus status, String createdBy, Instant createdAt,
                                      Instant updatedAt, List<ApprovalStepResponse> steps) {

    public static ApprovalRequestResponse summary(ApprovalRequest request) {
        return new ApprovalRequestResponse(request.getId(), request.getPaperId(), request.getPaperTitle(),
                request.getCurrentStage(), request.getStatus(), request.getCreatedBy(),
                request.getCreatedAt(), request.getUpdatedAt(), List.of());
    }

    public static ApprovalRequestResponse detail(ApprovalRequest request) {
        List<ApprovalStepResponse> steps = request.getSteps().stream()
                .map(ApprovalStepResponse::from)
                .toList();
        return new ApprovalRequestResponse(request.getId(), request.getPaperId(), request.getPaperTitle(),
                request.getCurrentStage(), request.getStatus(), request.getCreatedBy(),
                request.getCreatedAt(), request.getUpdatedAt(), steps);
    }
}
