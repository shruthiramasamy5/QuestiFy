package com.questify.questionbank.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
    name = "questions",
    indexes = {
        @Index(name = "idx_question_subject", columnList = "subject"),
        @Index(name = "idx_question_subject_id", columnList = "subject_id"),
        @Index(name = "idx_question_bloom", columnList = "bloom_level"),
        @Index(name = "idx_question_difficulty", columnList = "difficulty")
    }
)
public class Question extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "question_text", nullable = false, length = 4000)
    private String questionText;

    @Column(nullable = false, length = 128)
    private String subject;

    @Column(nullable = false, length = 64)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 24)
    private QuestionType questionType;

    @Column(nullable = false)
    private Integer marks;

    @Enumerated(EnumType.STRING)
    @Column(name = "bloom_level", nullable = false, length = 4)
    private BloomLevel bloomLevel;

    @Column(name = "created_by", nullable = false, length = 128)
    private String createdBy;

    @OneToMany(
        mappedBy = "question",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<QuestionCourseOutcome> courseOutcomes =
            new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public BloomLevel getBloomLevel() {
        return bloomLevel;
    }

    public void setBloomLevel(BloomLevel bloomLevel) {
        this.bloomLevel = bloomLevel;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Set<QuestionCourseOutcome> getCourseOutcomes() {
        return courseOutcomes;
    }

    public void setCourseOutcomes(
            Set<QuestionCourseOutcome> courseOutcomes) {
        this.courseOutcomes = courseOutcomes;
    }
}