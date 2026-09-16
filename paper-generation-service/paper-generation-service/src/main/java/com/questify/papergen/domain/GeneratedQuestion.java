package com.questify.papergen.domain;

/** A single question placed into a generated paper. */
public class GeneratedQuestion {

    private String questionId;
    private String text;
    private String courseOutcome;
    private String bloomLevel;
    private String difficulty;
    private Integer marks;
    private Integer sequence;

    public GeneratedQuestion() { }

    public GeneratedQuestion(String questionId, String text, String courseOutcome,
                             String bloomLevel, String difficulty, Integer marks, Integer sequence) {
        this.questionId = questionId;
        this.text = text;
        this.courseOutcome = courseOutcome;
        this.bloomLevel = bloomLevel;
        this.difficulty = difficulty;
        this.marks = marks;
        this.sequence = sequence;
    }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getCourseOutcome() { return courseOutcome; }
    public void setCourseOutcome(String courseOutcome) { this.courseOutcome = courseOutcome; }
    public String getBloomLevel() { return bloomLevel; }
    public void setBloomLevel(String bloomLevel) { this.bloomLevel = bloomLevel; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }
    public Integer getSequence() { return sequence; }
    public void setSequence(Integer sequence) { this.sequence = sequence; }
}
