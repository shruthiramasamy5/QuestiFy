package com.questify.papers.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreatePaperRequest {

    @NotBlank(message = "title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "subjectCode is required")
    private String subjectCode;

    private String subjectName;

    @NotBlank(message = "examType is required")
    private String examType;

    @NotNull(message = "totalMarks is required")
    @Min(value = 1, message = "totalMarks must be positive")
    private Integer totalMarks;

    @NotNull(message = "durationMinutes is required")
    @Min(value = 5, message = "durationMinutes must be at least 5")
    private Integer durationMinutes;

    private String generatedDraftId;
    private String institutionId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getGeneratedDraftId() { return generatedDraftId; }
    public void setGeneratedDraftId(String generatedDraftId) { this.generatedDraftId = generatedDraftId; }
    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
}
