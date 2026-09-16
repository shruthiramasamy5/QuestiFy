package com.questify.questionbank.repository;

import com.questify.questionbank.entity.DraftStatus;
import com.questify.questionbank.entity.QuestionDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionDraftRepository extends JpaRepository<QuestionDraft, Long> {

    List<QuestionDraft> findByStatus(DraftStatus status);

    List<QuestionDraft> findBySubjectAndStatus(String subject, DraftStatus status);
}
