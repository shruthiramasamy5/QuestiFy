package com.questify.papergen.service;

import com.questify.papergen.domain.GeneratedPaper;
import com.questify.papergen.domain.GeneratedQuestion;
import com.questify.papergen.repository.GeneratedPaperRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Prevents reusing questions that appeared in recent papers for the same subject. */
@Service
public class RepetitionCheckService {

    private final GeneratedPaperRepository repository;

    public RepetitionCheckService(GeneratedPaperRepository repository) {
        this.repository = repository;
    }

    public Set<String> previouslyUsedQuestionIds(String subjectCode, int windowDays) {
        Instant after = Instant.now().minus(Math.max(windowDays, 0), ChronoUnit.DAYS);
        List<GeneratedPaper> recent = repository.findBySubjectCodeAndCreatedAtAfter(subjectCode, after);
        Set<String> used = new HashSet<>();
        for (GeneratedPaper paper : recent) {
            for (GeneratedQuestion q : paper.getQuestions()) {
                if (q.getQuestionId() != null) {
                    used.add(q.getQuestionId());
                }
            }
        }
        return used;
    }
}
