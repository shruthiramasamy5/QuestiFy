package com.questify.questionbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "course_outcomes",
        uniqueConstraints = @UniqueConstraint(name = "uk_co_subject_code", columnNames = {"subject", "code"}))
public class CourseOutcome extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Course outcome code, e.g. CO1. */
    @Column(nullable = false, length = 16)
    private String code;

    @Column(nullable = false, length = 512)
    private String description;

    @Column(nullable = false, length = 128)
    private String subject;

    /** Target Bloom level for the outcome (optional). */
    @Enumerated(EnumType.STRING)
    @Column(name = "bloom_level", length = 4)
    private BloomLevel bloomLevel;

    @Column(name = "created_by", nullable = false, length = 128)
    private String createdBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public BloomLevel getBloomLevel() { return bloomLevel; }
    public void setBloomLevel(BloomLevel bloomLevel) { this.bloomLevel = bloomLevel; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
