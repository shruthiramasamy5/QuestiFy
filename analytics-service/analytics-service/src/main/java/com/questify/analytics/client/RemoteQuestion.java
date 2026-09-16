package com.questify.analytics.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Subset of the question-bank-service representation used for aggregation. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteQuestion {
    private Long id;
    private String courseOutcome;

    @JsonAlias("courseOutcomes")
    private List<RemoteCourseOutcome> courseOutcomes;

    private String bloomLevel;
    private String createdBy;

    public RemoteQuestion() {}

    public RemoteQuestion(Long id, String courseOutcome, String bloomLevel, String createdBy) {
        this.id = id;
        this.courseOutcome = courseOutcome;
        this.bloomLevel = bloomLevel;
        this.createdBy = createdBy;
    }

    public Long id() { return id; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String courseOutcome() {
        if (courseOutcome != null && !courseOutcome.isBlank()) {
            return courseOutcome;
        }
        if (courseOutcomes != null && !courseOutcomes.isEmpty() && courseOutcomes.get(0) != null) {
            return courseOutcomes.get(0).getCode();
        }
        return null;
    }
    public String getCourseOutcome() { return courseOutcome(); }
    public void setCourseOutcome(String courseOutcome) { this.courseOutcome = courseOutcome; }

    public List<RemoteCourseOutcome> getCourseOutcomes() { return courseOutcomes; }
    public void setCourseOutcomes(List<RemoteCourseOutcome> courseOutcomes) { this.courseOutcomes = courseOutcomes; }

    public String bloomLevel() { return bloomLevel; }
    public String getBloomLevel() { return bloomLevel; }
    public void setBloomLevel(String bloomLevel) { this.bloomLevel = bloomLevel; }

    public String createdBy() { return createdBy; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RemoteCourseOutcome {
        private String code;
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}
