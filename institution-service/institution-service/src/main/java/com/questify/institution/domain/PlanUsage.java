package com.questify.institution.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "plan_usage", uniqueConstraints = @UniqueConstraint(name = "uk_plan_usage_institution_period", columnNames = { "institution_id", "period" }))
public class PlanUsage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    /** Billing period in ISO yyyy-MM form. */
    @Column(nullable = false, length = 7)
    private String period;

    @Column(name = "papers_generated", nullable = false)
    private int papersGenerated = 0;

    @Column(name = "questions_created", nullable = false)
    private int questionsCreated = 0;

    @Column(name = "active_users", nullable = false)
    private int activeUsers = 0;

    @Column(name = "storage_used_mb", nullable = false)
    private int storageUsedMb = 0;

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

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getPapersGenerated() {
        return papersGenerated;
    }

    public void setPapersGenerated(int papersGenerated) {
        this.papersGenerated = papersGenerated;
    }

    public int getQuestionsCreated() {
        return questionsCreated;
    }

    public void setQuestionsCreated(int questionsCreated) {
        this.questionsCreated = questionsCreated;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    public int getStorageUsedMb() {
        return storageUsedMb;
    }

    public void setStorageUsedMb(int storageUsedMb) {
        this.storageUsedMb = storageUsedMb;
    }
}
