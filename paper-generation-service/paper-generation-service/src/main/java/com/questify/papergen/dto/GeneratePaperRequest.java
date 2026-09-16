package com.questify.papergen.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

/** Parameters accepted by POST /api/generate. */
public class GeneratePaperRequest {

    @NotBlank(message = "subjectCode is required")
    private String subjectCode;

    private String subjectName;

    @NotBlank(message = "examType is required")
    private String examType;

    @NotNull(message = "questionCount is required")
    @Min(value = 1, message = "questionCount must be at least 1")
    @Max(value = 200, message = "questionCount must not exceed 200")
    private Integer questionCount;

    @NotNull(message = "totalMarks is required")
    @Min(value = 1, message = "totalMarks must be at least 1")
    private Integer totalMarks;

    @NotNull(message = "durationMinutes is required")
    @Min(value = 5, message = "durationMinutes must be at least 5")
    private Integer durationMinutes;

    /** CO code -> number of questions, e.g. {"CO1": 3, "CO2": 2}. */
    private Map<String, Integer> coDistribution = new HashMap<>();

    /** Bloom level -> number of questions, e.g. {"REMEMBER": 2, "APPLY": 3}. */
    private Map<String, Integer> bloomDistribution = new HashMap<>();

    /** Difficulty -> number of questions, e.g. {"EASY": 2, "MEDIUM": 2, "HARD": 1}. */
    private Map<String, Integer> difficultyMix = new HashMap<>();

    /** Exclude questions used in papers generated within this many days. */
    @Min(value = 0, message = "repetitionWindowDays cannot be negative")
    private Integer repetitionWindowDays = 365;

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Map<String, Integer> getCoDistribution() { return coDistribution; }
    public void setCoDistribution(Map<String, Integer> coDistribution) { this.coDistribution = coDistribution; }
    public Map<String, Integer> getBloomDistribution() { return bloomDistribution; }
    public void setBloomDistribution(Map<String, Integer> bloomDistribution) { this.bloomDistribution = bloomDistribution; }
    public Map<String, Integer> getDifficultyMix() { return difficultyMix; }
    public void setDifficultyMix(Map<String, Integer> difficultyMix) { this.difficultyMix = difficultyMix; }
    public Integer getRepetitionWindowDays() { return repetitionWindowDays; }
    public void setRepetitionWindowDays(Integer repetitionWindowDays) { this.repetitionWindowDays = repetitionWindowDays; }
}
