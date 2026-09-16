package com.questify.papergen.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Question payload returned by QUESTION-BANK-SERVICE. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionDto {

    private String id;

    @JsonAlias({"questionText", "text"})
    private String text;

    @JsonAlias({"subject", "subjectCode"})
    private String subjectCode;

    private String courseOutcome;

    @JsonAlias("courseOutcomes")
    private List<CourseOutcomeItemDto> courseOutcomes;

    private String bloomLevel;
    private String difficulty;
    private Integer marks;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getCourseOutcome() {
        if (courseOutcome != null && !courseOutcome.isBlank()) {
            return courseOutcome;
        }
        if (courseOutcomes != null && !courseOutcomes.isEmpty() && courseOutcomes.get(0) != null) {
            return courseOutcomes.get(0).getCode();
        }
        return null;
    }
    public void setCourseOutcome(String courseOutcome) { this.courseOutcome = courseOutcome; }

    public List<CourseOutcomeItemDto> getCourseOutcomes() { return courseOutcomes; }
    public void setCourseOutcomes(List<CourseOutcomeItemDto> courseOutcomes) { this.courseOutcomes = courseOutcomes; }

    public String getBloomLevel() { return bloomLevel; }
    public void setBloomLevel(String bloomLevel) { this.bloomLevel = bloomLevel; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CourseOutcomeItemDto {
        private String code;
        private String description;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
