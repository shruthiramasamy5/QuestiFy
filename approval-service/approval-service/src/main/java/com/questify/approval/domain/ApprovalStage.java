package com.questify.approval.domain;

/**
 * Workflow stages of a QuestiFy paper approval:
 * FACULTY (submitted) -> HOD -> REVIEWER -> COMPLETED.
 */
public enum ApprovalStage {

    FACULTY("FACULTY"),
    HOD("HOD"),
    REVIEWER("REVIEWER"),
    COMPLETED(null);

    private final String approverRole;

    ApprovalStage(String approverRole) {
        this.approverRole = approverRole;
    }

    /** Role allowed to action a request currently sitting at this stage. */
    public String approverRole() {
        return approverRole;
    }

    /** Next stage after a successful approval. */
    public ApprovalStage next() {
        return switch (this) {
            case FACULTY -> HOD;
            case HOD -> REVIEWER;
            case REVIEWER, COMPLETED -> COMPLETED;
        };
    }

    public boolean isTerminal() {
        return this == COMPLETED;
    }
}
