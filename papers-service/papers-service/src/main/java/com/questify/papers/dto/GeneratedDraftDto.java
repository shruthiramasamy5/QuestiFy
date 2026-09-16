package com.questify.papers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/** Projection of the draft returned by PAPER-GENERATION-SERVICE. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneratedDraftDto {

    private String id;
    private String subjectCode;
    private String subjectName;
    private String examType;
    private Integer totalMarks;
    private Integer durationMinutes;
    private List<DraftQuestion> questions = new ArrayList<>();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DraftQuestion {
        private String questionId;
        private Integer marks;
        private Integer sequence;
        private String questionText;
        private String bloomLevel;
        private String courseOutcome;
        private String unit;
        private String difficulty;
        private String section;

        public String getQuestionId() { return questionId; }
        public void setQuestionId(String questionId) { this.questionId = questionId; }
        public Integer getMarks() { return marks; }
        public void setMarks(Integer marks) { this.marks = marks; }
        public Integer getSequence() { return sequence; }
        public void setSequence(Integer sequence) { this.sequence = sequence; }
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public String getBloomLevel() { return bloomLevel; }
        public void setBloomLevel(String bloomLevel) { this.bloomLevel = bloomLevel; }
        public String getCourseOutcome() { return courseOutcome; }
        public void setCourseOutcome(String courseOutcome) { this.courseOutcome = courseOutcome; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public List<DraftQuestion> getQuestions() { return questions; }
    public void setQuestions(List<DraftQuestion> questions) { this.questions = questions; }
}
