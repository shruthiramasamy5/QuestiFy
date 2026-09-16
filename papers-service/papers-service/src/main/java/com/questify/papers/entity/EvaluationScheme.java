package com.questify.papers.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Evaluation configuration attached to a finalized paper. */
@Entity
@Table(name = "evaluation_schemes")
public class EvaluationScheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paper_id", nullable = false, unique = true)
    private Paper paper;

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(name = "passing_marks")
    private Integer passingMarks;

    @Column(name = "negative_marking", nullable = false)
    private boolean negativeMarking = false;

    @Column(name = "partial_marking", nullable = false)
    private boolean partialMarking = true;

    /** Free-form JSON for institution specific evaluation configuration. */
    @Lob
    @Column(name = "configuration_json")
    private String configurationJson;

    @OneToMany(mappedBy = "evaluationScheme", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<QuestionRubric> rubrics = new ArrayList<>();

    @Column(name = "updated_by", length = 120)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    @Column(nullable = false, length = 200)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Paper getPaper() { return paper; }
    public void setPaper(Paper paper) { this.paper = paper; }
    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    public Integer getPassingMarks() { return passingMarks; }
    public void setPassingMarks(Integer passingMarks) { this.passingMarks = passingMarks; }
    public boolean isNegativeMarking() { return negativeMarking; }
    public void setNegativeMarking(boolean negativeMarking) { this.negativeMarking = negativeMarking; }
    public boolean isPartialMarking() { return partialMarking; }
    public void setPartialMarking(boolean partialMarking) { this.partialMarking = partialMarking; }
    public String getConfigurationJson() { return configurationJson; }
    public void setConfigurationJson(String configurationJson) { this.configurationJson = configurationJson; }
    public List<QuestionRubric> getRubrics() { return rubrics; }
    public void setRubrics(List<QuestionRubric> rubrics) { this.rubrics = rubrics; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
