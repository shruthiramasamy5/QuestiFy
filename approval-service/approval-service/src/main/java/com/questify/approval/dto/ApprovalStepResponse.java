package com.questify.approval.dto;

import com.questify.approval.domain.ApprovalStage;
import com.questify.approval.domain.ApprovalStatus;
import com.questify.approval.domain.ApprovalStep;
import java.time.Instant;

public record ApprovalStepResponse(Long id, ApprovalStage stage, String approverRole, String actionedBy,
                                   ApprovalStatus decision, String comment, Instant timestamp) {

    public static ApprovalStepResponse from(ApprovalStep step) {
        return new ApprovalStepResponse(step.getId(), step.getStage(), step.getApproverRole(),
                step.getActionedBy(), step.getDecision(), step.getComment(), step.getTimestamp());
    }
}
