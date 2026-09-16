package com.questify.questionbank.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Join entity mapping a question to a course outcome. */
@Entity
@Table(name = "question_course_outcomes",
        uniqueConstraints = @UniqueConstraint(name = "uk_question_co",
                columnNames = {"question_id", "course_outcome_id"}))
public class QuestionCourseOutcome extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_outcome_id", nullable = false)
    private CourseOutcome courseOutcome;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public CourseOutcome getCourseOutcome() { return courseOutcome; }
    public void setCourseOutcome(CourseOutcome courseOutcome) { this.courseOutcome = courseOutcome; }
}
