package com.questify.backup.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A named snapshot grouping a set of papers. Only metadata is stored here —
 * writing the actual archive to disk / object storage is out of scope, the
 * {@code storageReference} is the agreed pointer for whatever performs it.
 */
@Entity
@Table(name = "backup_sets")
public class BackupSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String label;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "backup_set_papers", joinColumns = @JoinColumn(name = "backup_set_id"))
    @Column(name = "paper_id", nullable = false)
    private List<Long> paperIds = new ArrayList<>();

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;

    @Column(name = "institution_id", length = 64)
    private String institutionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Logical path/URI of the archive, e.g. {@code s3://questify-backups/2026/07/set-12.zip}. */
    @Column(name = "storage_reference", nullable = false, length = 500)
    private String storageReference;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Long> getPaperIds() {
        return paperIds;
    }

    public void setPaperIds(List<Long> paperIds) {
        this.paperIds = paperIds;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getStorageReference() {
        return storageReference;
    }

    public void setStorageReference(String storageReference) {
        this.storageReference = storageReference;
    }
}
