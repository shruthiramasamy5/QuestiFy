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

/** Marks distribution and rubric for a single question of a paper. */
@Entity
@Table(name = "question_rubrics")
public class QuestionRubric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluation_scheme_id", nullable = false)
    private EvaluationScheme evaluationScheme;

    @Column(name = "question_id", length = 64)
    private String questionId;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    @Column(nullable = false)
    private Integer marks;

    @Column(name = "criteria", length = 1000)
    private String criteria;

    @Column(name = "step_marking", length = 1000)
    private String stepMarking;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EvaluationScheme getEvaluationScheme() { return evaluationScheme; }
    public void setEvaluationScheme(EvaluationScheme evaluationScheme) { this.evaluationScheme = evaluationScheme; }
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
