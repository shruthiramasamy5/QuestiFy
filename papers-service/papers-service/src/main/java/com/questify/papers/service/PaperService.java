package com.questify.papers.service;

import com.questify.papers.client.PaperGenerationClient;
import com.questify.papers.dto.CreatePaperFromDraftRequest;
import com.questify.papers.dto.CreatePaperRequest;
import com.questify.papers.dto.EvaluationSchemeRequest;
import com.questify.papers.dto.GeneratedDraftDto;
import com.questify.papers.entity.EvaluationScheme;
import com.questify.papers.entity.Paper;
import com.questify.papers.entity.PaperStatus;
import com.questify.papers.entity.QuestionRubric;
import com.questify.papers.exception.BusinessRuleException;
import com.questify.papers.exception.ResourceNotFoundException;
import com.questify.papers.repository.EvaluationSchemeRepository;
import com.questify.papers.repository.PaperRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaperService {

    private final PaperRepository paperRepository;
    private final EvaluationSchemeRepository schemeRepository;
    private final PaperGenerationClient generationClient;

    public PaperService(PaperRepository paperRepository,
                        EvaluationSchemeRepository schemeRepository,
                        PaperGenerationClient generationClient) {
        this.paperRepository = paperRepository;
        this.schemeRepository = schemeRepository;
        this.generationClient = generationClient;
    }

    @Transactional(readOnly = true)
    public List<Paper> findAll(String subjectCode, PaperStatus status) {
        if (subjectCode != null && !subjectCode.isBlank()) {
            return paperRepository.findBySubjectCode(subjectCode);
        }
        if (status != null) {
            return paperRepository.findByStatus(status);
        }
        return paperRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Paper findById(Long id) {
        return paperRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paper not found: " + id));
    }

    @Transactional
    public Paper create(CreatePaperRequest request, String username) {
        if (request.getGeneratedDraftId() != null
                && paperRepository.existsByGeneratedDraftId(request.getGeneratedDraftId())) {
            throw new BusinessRuleException("A paper already exists for draft " + request.getGeneratedDraftId());
        }
        Paper paper = new Paper();
        paper.setTitle(request.getTitle());
        paper.setSubjectCode(request.getSubjectCode());
        paper.setSubjectName(request.getSubjectName());
        paper.setSubject(request.getSubjectName());
        paper.setExamType(request.getExamType());
        paper.setTotalMarks(request.getTotalMarks());
        paper.setDurationMinutes(request.getDurationMinutes());
        paper.setGeneratedDraftId(request.getGeneratedDraftId());
        paper.setInstitutionId(request.getInstitutionId());
        paper.setStatus(PaperStatus.DRAFT);
        paper.setCreatedBy(username);
        return paperRepository.save(paper);
    }

    @Transactional
    public Paper createFromDraft(CreatePaperFromDraftRequest request, String username, String bearerToken) {
        if (paperRepository.existsByGeneratedDraftId(request.getDraftId())) {
            throw new BusinessRuleException("A paper already exists for draft " + request.getDraftId());
        }
        GeneratedDraftDto draft = generationClient.fetchDraft(request.getDraftId(), bearerToken);
        if (draft == null || draft.getId() == null) {
            throw new ResourceNotFoundException("Draft not found: " + request.getDraftId());
        }

        Paper paper = new Paper();
        paper.setTitle(request.getTitle() != null && !request.getTitle().isBlank()
                ? request.getTitle()
                : draft.getSubjectCode() + " - " + draft.getExamType());
        paper.setSubjectCode(draft.getSubjectCode());
        paper.setSubjectName(draft.getSubjectName());
        paper.setSubject(draft.getSubjectName());
        paper.setExamType(draft.getExamType());
        paper.setTotalMarks(draft.getTotalMarks());
        paper.setDurationMinutes(draft.getDurationMinutes());
        paper.setGeneratedDraftId(draft.getId());
        paper.setInstitutionId(request.getInstitutionId());
        paper.setStatus(PaperStatus.DRAFT);
        paper.setCreatedBy(username);
        Paper saved = paperRepository.save(paper);

        if (draft.getQuestions() != null && !draft.getQuestions().isEmpty()) {
            List<com.questify.papers.entity.PaperQuestion> pqs = new ArrayList<>();
            for (GeneratedDraftDto.DraftQuestion q : draft.getQuestions()) {
                Long qId = null;
                try {
                    if (q.getQuestionId() != null) qId = Long.parseLong(q.getQuestionId());
                } catch (Exception ignored) {}

                com.questify.papers.entity.PaperQuestion pq = new com.questify.papers.entity.PaperQuestion(
                        saved,
                        qId,
                        q.getSequence() != null ? q.getSequence() : (pqs.size() + 1),
                        q.getMarks() != null ? q.getMarks() : 2,
                        q.getSection() != null ? q.getSection() : "Part A",
                        q.getQuestionText() != null ? q.getQuestionText() : ("Question " + (q.getSequence() != null ? q.getSequence() : (pqs.size() + 1))),
                        q.getBloomLevel() != null ? q.getBloomLevel() : "K2",
                        q.getCourseOutcome() != null ? q.getCourseOutcome() : "CO1",
                        q.getUnit() != null ? q.getUnit() : "Unit 1",
                        q.getDifficulty() != null ? q.getDifficulty() : "MEDIUM"
                );
                pqs.add(pq);
            }
            saved.getQuestions().addAll(pqs);
            saved = paperRepository.save(saved);

            EvaluationScheme scheme = new EvaluationScheme();
            scheme.setName("Default Evaluation Scheme");
            scheme.setPaper(saved);
            scheme.setTotalMarks(draft.getTotalMarks());
            scheme.setUpdatedBy(username);
            scheme.setUpdatedAt(Instant.now());
            List<QuestionRubric> rubrics = new ArrayList<>();
            for (GeneratedDraftDto.DraftQuestion q : draft.getQuestions()) {
                QuestionRubric rubric = new QuestionRubric();
                rubric.setEvaluationScheme(scheme);
                rubric.setQuestionId(q.getQuestionId());
                rubric.setQuestionNumber(q.getSequence());
                rubric.setMarks(q.getMarks() == null ? 0 : q.getMarks());
                rubrics.add(rubric);
            }
            scheme.setRubrics(rubrics);
            schemeRepository.save(scheme);
            saved.setEvaluationScheme(scheme);
        }
        return saved;
    }

    @Transactional
    public Paper updateStatus(Long id, PaperStatus newStatus, String username) {
        Paper paper = findById(id);
        paper.setStatus(newStatus);
        paper.setUpdatedAt(Instant.now());
        return paperRepository.save(paper);
    }

    @Transactional
    public EvaluationScheme upsertEvaluationScheme(Long paperId, EvaluationSchemeRequest request, String username) {
        Paper paper = findById(paperId);
        validateScheme(paper, request);

        EvaluationScheme scheme = schemeRepository.findByPaperId(paperId).orElseGet(() -> {
            EvaluationScheme s = new EvaluationScheme();
            s.setPaper(paper);
            return s;
        });

        scheme.setTotalMarks(request.getTotalMarks());
        scheme.setPassingMarks(request.getPassingMarks());
        scheme.setNegativeMarking(request.isNegativeMarking());
        scheme.setPartialMarking(request.isPartialMarking());
        scheme.setConfigurationJson(request.getConfigurationJson());
        scheme.setUpdatedBy(username);
        scheme.setUpdatedAt(Instant.now());

        scheme.getRubrics().clear();
        for (EvaluationSchemeRequest.RubricRequest r : request.getRubrics()) {
            QuestionRubric rubric = new QuestionRubric();
            rubric.setEvaluationScheme(scheme);
            rubric.setQuestionId(r.getQuestionId());
            rubric.setQuestionNumber(r.getQuestionNumber());
            rubric.setMarks(r.getMarks());
            rubric.setCriteria(r.getCriteria());
            rubric.setStepMarking(r.getStepMarking());
            scheme.getRubrics().add(rubric);
        }

        EvaluationScheme saved = schemeRepository.save(scheme);
        paper.setEvaluationScheme(saved);
        paper.setUpdatedAt(Instant.now());
        paperRepository.save(paper);
        return saved;
    }

    private void validateScheme(Paper paper, EvaluationSchemeRequest request) {
        if (!request.getTotalMarks().equals(paper.getTotalMarks())) {
            throw new BusinessRuleException("Evaluation scheme totalMarks (" + request.getTotalMarks()
                    + ") must match the paper total marks (" + paper.getTotalMarks() + ")");
        }
        if (request.getPassingMarks() != null && request.getPassingMarks() > request.getTotalMarks()) {
            throw new BusinessRuleException("passingMarks cannot exceed totalMarks");
        }
        if (!request.getRubrics().isEmpty()) {
            int rubricTotal = request.getRubrics().stream().mapToInt(r -> r.getMarks() == null ? 0 : r.getMarks()).sum();
            if (rubricTotal != request.getTotalMarks()) {
                throw new BusinessRuleException("Rubric marks total (" + rubricTotal
                        + ") must equal totalMarks (" + request.getTotalMarks() + ")");
            }
            long distinct = request.getRubrics().stream().map(EvaluationSchemeRequest.RubricRequest::getQuestionNumber)
                    .distinct().count();
            if (distinct != request.getRubrics().size()) {
                throw new BusinessRuleException("Duplicate questionNumber values in rubrics");
            }
        }
    }
}
