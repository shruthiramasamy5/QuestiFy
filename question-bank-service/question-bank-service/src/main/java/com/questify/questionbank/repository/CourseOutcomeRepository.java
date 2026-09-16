package com.questify.questionbank.repository;

import com.questify.questionbank.entity.CourseOutcome;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseOutcomeRepository extends JpaRepository<CourseOutcome, Long> {

    Optional<CourseOutcome> findBySubjectIgnoreCaseAndCodeIgnoreCase(String subject, String code);

    boolean existsBySubjectIgnoreCaseAndCodeIgnoreCase(String subject, String code);

    Page<CourseOutcome> findBySubjectIgnoreCase(String subject, Pageable pageable);
}
