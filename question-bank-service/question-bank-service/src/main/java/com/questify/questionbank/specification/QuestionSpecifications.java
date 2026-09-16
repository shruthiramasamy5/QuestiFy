package com.questify.questionbank.specification;

import com.questify.questionbank.dto.QuestionSearchCriteria;
import com.questify.questionbank.entity.CourseOutcome;
import com.questify.questionbank.entity.Question;
import com.questify.questionbank.entity.QuestionCourseOutcome;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class QuestionSpecifications {

    private QuestionSpecifications() {
    }

    public static Specification<Question> withCriteria(QuestionSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(criteria.subject())) {
                predicates.add(cb.equal(cb.lower(root.get("subject")), criteria.subject().toLowerCase()));
            }
            if (StringUtils.hasText(criteria.unit())) {
                predicates.add(cb.equal(cb.lower(root.get("unit")), criteria.unit().toLowerCase()));
            }
            if (criteria.bloomLevel() != null) {
                predicates.add(cb.equal(root.get("bloomLevel"), criteria.bloomLevel()));
            }
            if (criteria.difficulty() != null) {
                predicates.add(cb.equal(root.get("difficulty"), criteria.difficulty()));
            }
            if (criteria.questionType() != null) {
                predicates.add(cb.equal(root.get("questionType"), criteria.questionType()));
            }
            if (criteria.marks() != null) {
                predicates.add(cb.equal(root.get("marks"), criteria.marks()));
            }
            if (StringUtils.hasText(criteria.search())) {
                predicates.add(cb.like(cb.lower(root.get("questionText")),
                        "%" + criteria.search().toLowerCase() + "%"));
            }
            if (criteria.courseOutcomeId() != null || StringUtils.hasText(criteria.courseOutcomeCode())) {
                Join<Question, QuestionCourseOutcome> mapping = root.join("courseOutcomes");
                Join<QuestionCourseOutcome, CourseOutcome> co = mapping.join("courseOutcome");
                if (criteria.courseOutcomeId() != null) {
                    predicates.add(cb.equal(co.get("id"), criteria.courseOutcomeId()));
                }
                if (StringUtils.hasText(criteria.courseOutcomeCode())) {
                    predicates.add(cb.equal(cb.lower(co.get("code")), criteria.courseOutcomeCode().toLowerCase()));
                }
                if (query != null) {
                    query.distinct(true);
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
