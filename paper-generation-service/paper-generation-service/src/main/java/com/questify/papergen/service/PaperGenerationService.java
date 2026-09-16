package com.questify.papergen.service;

import com.questify.papergen.ai.AiPaperGenerationProvider;
import com.questify.papergen.ai.AiProviderNotConfiguredException;
import com.questify.papergen.client.QuestionBankClient;
import com.questify.papergen.domain.GeneratedPaper;
import com.questify.papergen.domain.GeneratedQuestion;
import com.questify.papergen.domain.GenerationMetadata;
import com.questify.papergen.dto.GeneratePaperRequest;
import com.questify.papergen.dto.QuestionDto;
import com.questify.papergen.exception.GenerationException;
import com.questify.papergen.exception.ResourceNotFoundException;
import com.questify.papergen.repository.GeneratedPaperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class PaperGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PaperGenerationService.class);

    private final QuestionBankClient questionBankClient;
    private final QuestionSelectionService selectionService;
    private final RepetitionCheckService repetitionCheckService;
    private final GeneratedPaperRepository repository;
    private final AiPaperGenerationProvider aiProvider;

    public PaperGenerationService(QuestionBankClient questionBankClient,
                                  QuestionSelectionService selectionService,
                                  RepetitionCheckService repetitionCheckService,
                                  GeneratedPaperRepository repository,
                                  AiPaperGenerationProvider aiProvider) {
        this.questionBankClient = questionBankClient;
        this.selectionService = selectionService;
        this.repetitionCheckService = repetitionCheckService;
        this.repository = repository;
        this.aiProvider = aiProvider;
    }

    public GeneratedPaper generate(GeneratePaperRequest request, String username, String bearerToken) {
        validate(request);

        List<QuestionDto> candidates = questionBankClient.fetchCandidates(request.getSubjectCode(), bearerToken);
        if (candidates == null) {
            candidates = new ArrayList<>();
        }
        int poolSize = candidates.size();

        Set<String> used = repetitionCheckService.previouslyUsedQuestionIds(
                request.getSubjectCode(),
                request.getRepetitionWindowDays() == null ? 365 : request.getRepetitionWindowDays());

        List<QuestionDto> filtered = new ArrayList<>(candidates.stream()
                .filter(q -> q.getId() != null && !used.contains(q.getId()))
                .toList());
        int exclusions = poolSize - filtered.size();

        if (filtered.size() < request.getQuestionCount()) {
            log.warn("Repetition filter left {} questions for {}, relaxing to full pool",
                    filtered.size(), request.getSubjectCode());
            filtered = new ArrayList<>(candidates);
            exclusions = 0;
        }

        List<GeneratedQuestion> questions = null;
        String strategy = "RULE_BASED";

        if (aiProvider.isAvailable()) {
            try {
                List<QuestionDto> aiChosen = aiProvider.selectQuestions(request, filtered);
                questions = selectionService.toGeneratedQuestions(aiChosen, request.getQuestionCount());
                strategy = "AI_ASSISTED";
            } catch (AiProviderNotConfiguredException | GenerationException ex) {
                log.warn("AI-assisted selection unavailable/failed for {}, falling back to rule-based: {}",
                        request.getSubjectCode(), ex.getMessage());
            }
        }

        if (questions == null) {
            questions = selectionService.select(request, filtered);
        }

        GeneratedPaper paper = new GeneratedPaper();
        paper.setSubjectCode(request.getSubjectCode());
        paper.setSubjectName(request.getSubjectName());
        paper.setExamType(request.getExamType());
        paper.setTotalMarks(request.getTotalMarks());
        paper.setDurationMinutes(request.getDurationMinutes());
        paper.setQuestionCount(questions.size());
        paper.setQuestions(questions);
        paper.setCreatedBy(username);
        paper.setCreatedAt(Instant.now());
        paper.setUpdatedAt(Instant.now());

        GenerationMetadata meta = new GenerationMetadata();
        meta.setRequestedCoDistribution(request.getCoDistribution());
        meta.setRequestedBloomDistribution(request.getBloomDistribution());
        meta.setRequestedDifficultyMix(request.getDifficultyMix());
        meta.setAchievedCoDistribution(selectionService.tally(questions, GeneratedQuestion::getCourseOutcome));
        meta.setAchievedBloomDistribution(selectionService.tally(questions, GeneratedQuestion::getBloomLevel));
        meta.setAchievedDifficultyMix(selectionService.tally(questions, GeneratedQuestion::getDifficulty));
        meta.setCandidatePoolSize(poolSize);
        meta.setRepetitionExclusions(exclusions);
        meta.setStrategy(strategy);
        paper.setMetadata(meta);

        return repository.save(paper);
    }

    public GeneratedPaper findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Generated paper not found: " + id));
    }

    public List<GeneratedPaper> findAll(String subjectCode) {
        if (subjectCode != null && !subjectCode.isBlank()) {
            return repository.findBySubjectCode(subjectCode);
        }
        return repository.findAll();
    }

    private void validate(GeneratePaperRequest request) {
        int coSum = sum(request.getCoDistribution());
        int bloomSum = sum(request.getBloomDistribution());
        int diffSum = sum(request.getDifficultyMix());
        int count = request.getQuestionCount();

        if (coSum > count) {
            throw new GenerationException("CO distribution total (" + coSum + ") exceeds questionCount (" + count + ")");
        }
        if (bloomSum > count) {
            throw new GenerationException("Bloom distribution total (" + bloomSum + ") exceeds questionCount (" + count + ")");
        }
        if (diffSum > count) {
            throw new GenerationException("Difficulty mix total (" + diffSum + ") exceeds questionCount (" + count + ")");
        }
    }

    private int sum(java.util.Map<String, Integer> map) {
        if (map == null) {
            return 0;
        }
        return map.values().stream().filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).sum();
    }
}
