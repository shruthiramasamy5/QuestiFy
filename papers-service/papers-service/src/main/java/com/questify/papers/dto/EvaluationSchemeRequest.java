package com.questify.papers.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EvaluationSchemeRequest {

    @NotNull(message = "totalMarks is required")
    @Min(value = 1, message = "totalMarks must be positive")
    private Integer totalMarks;

    @Min(value = 0, message = "passingMarks cannot be negative")
    private Integer passingMarks;

    private boolean negativeMarking = false;
    private boolean partialMarking = true;

    /** Optional institution specific configuration, stored as JSON. */
    private String configurationJson;

    @Valid
    private List<RubricRequest> rubrics = new ArrayList<>();

    public static class RubricRequest {
        private String questionId;

        @NotNull(message = "questionNumber is required")
        @Min(value = 1, message = "questionNumber must be positive")
        private Integer questionNumber;

        @NotNull(message = "marks is required")
        @Min(value = 0, message = "marks cannot be negative")
        private Integer marks;

        private String criteria;
        private String stepMarking;

        public String getQuestionId() { return questionId; }
        public void setQuestionId(String questionId) { this.questionId = questionId; }
        public Integer getQuestionNumber() { return questionNumber; }
        public void setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; }
        public Integer getMarks() { return marks; }
        public void setMarks(Integer marks) { this.marks = marks; }
        public String getCriteria() { return criteria; }
        public void setCriteria(String criteria) { this.criteria = criteria; }
        public String getStepMarking() { return stepMarking; }
        public void setStepMarking(String stepMarking) { this.stepMarking = stepMarking; }
    }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    public Integer getPassingMarks() { return passingMarks; }
    public void setPassingMarks(Integer passingMarks) { this.passingMarks = passingMarks; }
    public boolean isNegativeMarking() { return negativeMarking; }
    public void setNegativeMarking(boolean negativeMarking) { this.negativeMarking = negativeMarking; }
    public boolean isPartialMarking() { return partialMarking; }
    public void setPartialMarking(boolean partialMarking) { this.partialMarking = partialMarking; }
    public String getConfigurationJson() { return configurationJson; }
    public void setConfigurationJson(String configurationJson) { this.configurationJson = configurationJson; }
    public List<RubricRequest> getRubrics() { return rubrics; }
    public void setRubrics(List<RubricRequest> rubrics) { this.rubrics = rubrics; }
}
