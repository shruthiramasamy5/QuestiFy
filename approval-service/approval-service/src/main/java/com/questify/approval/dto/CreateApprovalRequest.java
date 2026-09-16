package com.questify.approval.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Payload used by paper-generation-service / faculty UI to open a workflow. */
public record CreateApprovalRequest(@NotNull Long paperId,
                                    @Size(max = 250) String paperTitle) {
}
