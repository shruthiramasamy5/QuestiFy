package com.questify.approval.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApprovalStageTest {

    @Test
    void workflowGoesFacultyThenHodThenReviewer() {
        assertThat(ApprovalStage.FACULTY.next()).isEqualTo(ApprovalStage.HOD);
        assertThat(ApprovalStage.HOD.next()).isEqualTo(ApprovalStage.REVIEWER);
        assertThat(ApprovalStage.REVIEWER.next()).isEqualTo(ApprovalStage.COMPLETED);
        assertThat(ApprovalStage.COMPLETED.isTerminal()).isTrue();
    }

    @Test
    void approverRoleMatchesStage() {
        assertThat(ApprovalStage.HOD.approverRole()).isEqualTo("HOD");
        assertThat(ApprovalStage.REVIEWER.approverRole()).isEqualTo("REVIEWER");
        assertThat(ApprovalStage.COMPLETED.approverRole()).isNull();
    }
}
