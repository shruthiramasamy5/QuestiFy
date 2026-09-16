package com.questify.papers.dto;

import jakarta.validation.constraints.NotBlank;

/** Finalizes a draft produced by paper-generation-service. */
public class CreatePaperFromDraftRequest {

    @NotBlank(message = "draftId is required")
    private String draftId;

    /** Optional override; defaults to the draft subject + exam type. */
    private String title;

    private String institutionId;

    public String getDraftId() { return draftId; }
    public void setDraftId(String draftId) { this.draftId = draftId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
}
