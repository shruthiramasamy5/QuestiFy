package com.questify.papergen.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A generated (draft) question paper persisted in MongoDB. */
@Document(collection = "generated_papers")
public class GeneratedPaper {

    @Id
    private String id;

    @Indexed
    private String subjectCode;
    private String subjectName;
    private String examType;
    private Integer totalMarks;
    private Integer durationMinutes;
    private Integer questionCount;

    private DraftStatus status = DraftStatus.DRAFT;

    private List<GeneratedQuestion> questions = new ArrayList<>();

    private GenerationMetadata metadata = new GenerationMetadata();

    @Indexed
    private String createdBy;
    private String institutionId;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
    public DraftStatus getStatus() { return status; }
    public void setStatus(DraftStatus status) { this.status = status; }
    public List<GeneratedQuestion> getQuestions() { return questions; }
    public void setQuestions(List<GeneratedQuestion> questions) { this.questions = questions; }
    public GenerationMetadata getMetadata() { return metadata; }
    public void setMetadata(GenerationMetadata metadata) { this.metadata = metadata; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
