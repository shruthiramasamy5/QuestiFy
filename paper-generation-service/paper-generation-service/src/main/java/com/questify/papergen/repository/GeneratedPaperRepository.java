package com.questify.papergen.repository;

import com.questify.papergen.domain.GeneratedPaper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface GeneratedPaperRepository extends MongoRepository<GeneratedPaper, String> {

    List<GeneratedPaper> findBySubjectCodeAndCreatedAtAfter(String subjectCode, Instant after);

    List<GeneratedPaper> findBySubjectCode(String subjectCode);

    List<GeneratedPaper> findByCreatedBy(String createdBy);
}
