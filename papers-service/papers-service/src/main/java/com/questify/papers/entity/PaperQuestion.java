package com.questify.papers.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "paper_questions")
public class PaperQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "question_order")
    private Integer questionOrder;

    @Column(nullable = false)
    private Integer marks = 2;

    @Column(length = 50)
    private String section;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "bloom_level", length = 10)
    private String bloomLevel;

    @Column(name = "course_outcome", length = 20)
    private String courseOutcome;

    @Column(length = 64)
    private String unit;

    @Column(length = 20)
    private String difficulty;

    public PaperQuestion() {
    }

    public PaperQuestion(Paper paper, Long questionId, Integer questionOrder, Integer marks,
                         String section, String questionText, String bloomLevel,
                         String courseOutcome, String unit, String difficulty) {
        this.paper = paper;
        this.questionId = questionId;
        this.questionOrder = questionOrder;
        this.marks = marks;
        this.section = section;
        this.questionText = questionText;
        this.bloomLevel = bloomLevel;
        this.courseOutcome = courseOutcome;
        this.unit = unit;
        this.difficulty = difficulty;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Paper getPaper() { return paper; }
    public void setPaper(Paper paper) { this.paper = paper; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }
    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
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
}
