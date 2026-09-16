package com.questify.questionbank.repository;

import com.questify.questionbank.entity.QuestionCourseOutcome;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionCourseOutcomeRepository extends JpaRepository<QuestionCourseOutcome, Long> {

    List<QuestionCourseOutcome> findByQuestionId(Long questionId);

    void deleteByQuestionIdAndCourseOutcomeId(Long questionId, Long courseOutcomeId);

    boolean existsByCourseOutcomeId(Long courseOutcomeId);
}
