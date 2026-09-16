package com.questify.papers.repository;

import com.questify.papers.entity.Paper;
import com.questify.papers.entity.PaperStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Long> {

    List<Paper> findBySubjectCode(String subjectCode);

    List<Paper> findByStatus(PaperStatus status);

    List<Paper> findByCreatedBy(String createdBy);

    Optional<Paper> findByGeneratedDraftId(String generatedDraftId);

    boolean existsByGeneratedDraftId(String generatedDraftId);
}
