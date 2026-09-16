package com.questify.papers.repository;

import com.questify.papers.entity.EvaluationScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluationSchemeRepository extends JpaRepository<EvaluationScheme, Long> {
    Optional<EvaluationScheme> findByPaperId(Long paperId);
}
