package com.questify.questionbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * An AI-drafted candidate question awaiting faculty review. Deliberately
 * a separate table from {@link Question} - a draft only becomes a real,
 * usable Question once a human approves it (see QuestionDraftService).
 */
@Entity
@Table(name = "question_drafts", indexes = {
        @Index(name = "idx_draft_status", columnList = "status"),
        @Index(name = "idx_draft_subject", columnList = "subject")
})
public class QuestionDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", nullable = false, length = 4000)
    private String questionText;

    @Column(nullable = false, length = 128)
    private String subject;

    @Column(nullable = false, length = 64)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Difficulty difficulty;

    @Column(nullable = false)
    private Integer marks;

    @Enumerated(EnumType.STRING)
    @Column(name = "bloom_level", nullable = false, length = 4)
    private BloomLevel bloomLevel;

    /** Single target CO for this draft; approval can still attach more via DraftApprovalRequest. */
    @Column(name = "course_outcome_id")
    private Long courseOutcomeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DraftStatus status = DraftStatus.PENDING_REVIEW;

    /** 0.0-1.0 model self-estimate of fit to the request; never treated as ground truth. */
    @Column
    private Double confidence;

    @Column(name = "ai_model", length = 64)
    private String aiModel;

    @Column(name = "created_by", nullable = false, length = 128)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "reviewed_by", length = 128)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "approved_question_id")
    private Long approvedQuestionId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }
    public BloomLevel getBloomLevel() { return bloomLevel; }
    public void setBloomLevel(BloomLevel bloomLevel) { this.bloomLevel = bloomLevel; }
    public Long getCourseOutcomeId() { return courseOutcomeId; }
    public void setCourseOutcomeId(Long courseOutcomeId) { this.courseOutcomeId = courseOutcomeId; }
    public DraftStatus getStatus() { return status; }
    public void setStatus(DraftStatus status) { this.status = status; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public Long getApprovedQuestionId() { return approvedQuestionId; }
    public void setApprovedQuestionId(Long approvedQuestionId) { this.approvedQuestionId = approvedQuestionId; }
}
