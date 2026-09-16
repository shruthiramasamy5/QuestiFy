package com.questify.questionbank.service;

import com.questify.questionbank.dto.CourseOutcomeResponse;
import com.questify.questionbank.dto.QuestionResponse;
import com.questify.questionbank.entity.CourseOutcome;
import com.questify.questionbank.entity.Question;
import com.questify.questionbank.entity.QuestionCourseOutcome;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class QuestionMapper {

    public CourseOutcomeResponse toResponse(CourseOutcome co) {
        return new CourseOutcomeResponse(co.getId(), co.getCode(), co.getDescription(), co.getSubject(),
                co.getBloomLevel(), co.getCreatedBy(), co.getCreatedAt(), co.getUpdatedAt());
    }

    public QuestionResponse toResponse(Question question) {
        List<CourseOutcomeResponse> outcomes = question.getCourseOutcomes().stream()
                .map(QuestionCourseOutcome::getCourseOutcome)
                .sorted(Comparator.comparing(CourseOutcome::getCode))
                .map(this::toResponse)
                .toList();
        return new QuestionResponse(question.getId(), question.getQuestionText(), question.getSubject(),
                question.getUnit(), question.getDifficulty(), question.getQuestionType(), question.getMarks(),
                question.getBloomLevel(), outcomes, question.getCreatedBy(), question.getCreatedAt(),
                question.getUpdatedAt());
    }
}
