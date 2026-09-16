package com.questify.papers.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

/** A finalized question paper. */
@Entity
@Table(name = "papers")
public class Paper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "subject_code", nullable = false, length = 50)
    private String subjectCode;

    @Column(name = "subject_name", length = 200)
    private String subjectName;
    @Column(nullable = false, length = 128)
    private String subject;

    @Column(name = "exam_type", nullable = false, length = 50)
    private String examType;

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /**
     * Reference to the draft produced by paper-generation-service.
     * This service never reads that service's database directly.
     */
    @Column(name = "generated_draft_id", length = 64)
    private String generatedDraftId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaperStatus status = PaperStatus.DRAFT;

    @Column(name = "institution_id", length = 64)
    private String institutionId;

    @Column(name = "created_by", nullable = false, length = 120)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToOne(mappedBy = "paper", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private EvaluationScheme evaluationScheme;

    @jakarta.persistence.OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @jakarta.persistence.OrderBy("questionOrder ASC")
    private java.util.List<PaperQuestion> questions = new java.util.ArrayList<>();

    @PreUpdate
    public void touch() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getGeneratedDraftId() { return generatedDraftId; }
    public void setGeneratedDraftId(String generatedDraftId) { this.generatedDraftId = generatedDraftId; }
    public PaperStatus getStatus() { return status; }
    public void setStatus(PaperStatus status) { this.status = status; }
    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public EvaluationScheme getEvaluationScheme() { return evaluationScheme; }
    public void setEvaluationScheme(EvaluationScheme evaluationScheme) { this.evaluationScheme = evaluationScheme; }
    public java.util.List<PaperQuestion> getQuestions() { return questions; }
    public void setQuestions(java.util.List<PaperQuestion> questions) { this.questions = questions; }
}
