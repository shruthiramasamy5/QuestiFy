package com.questify.questionbank.service;

import com.questify.questionbank.client.AiServiceClient;
import com.questify.questionbank.dto.DraftApprovalRequest;
import com.questify.questionbank.dto.DraftGenerationRequest;
import com.questify.questionbank.dto.QuestionDraftResponse;
import com.questify.questionbank.dto.QuestionRequest;
import com.questify.questionbank.dto.QuestionResponse;
import com.questify.questionbank.dto.ai.AiDraftQuestionsRequest;
import com.questify.questionbank.dto.ai.AiDraftQuestionsResponse;
import com.questify.questionbank.dto.ai.AiQuestionDraft;
import com.questify.questionbank.entity.BloomLevel;
import com.questify.questionbank.entity.CourseOutcome;
import com.questify.questionbank.entity.Difficulty;
import com.questify.questionbank.entity.DraftStatus;
import com.questify.questionbank.entity.QuestionDraft;
import com.questify.questionbank.exception.ResourceNotFoundException;
import com.questify.questionbank.repository.CourseOutcomeRepository;
import com.questify.questionbank.repository.QuestionDraftRepository;
import com.questify.questionbank.security.CurrentUser;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class QuestionDraftService {

    private final QuestionDraftRepository draftRepository;
    private final CourseOutcomeRepository courseOutcomeRepository;
    private final AiServiceClient aiServiceClient;
    private final QuestionService questionService;
    private final CurrentUser currentUser;

    public QuestionDraftService(
            QuestionDraftRepository draftRepository,
            CourseOutcomeRepository courseOutcomeRepository,
            AiServiceClient aiServiceClient,
            QuestionService questionService,
            CurrentUser currentUser) {

        this.draftRepository = draftRepository;
        this.courseOutcomeRepository = courseOutcomeRepository;
        this.aiServiceClient = aiServiceClient;
        this.questionService = questionService;
        this.currentUser = currentUser;
    }

    @Transactional
    public List<QuestionDraftResponse> generate(
            DraftGenerationRequest request,
            String bearerToken) {

        String coDescription = null;

        if (request.courseOutcomeId() != null) {

            CourseOutcome co =
                    courseOutcomeRepository
                            .findById(request.courseOutcomeId())
                            .orElseThrow(
                                    () -> new ResourceNotFoundException(
                                            "Course outcome "
                                                    + request.courseOutcomeId()
                                                    + " was not found"
                                    )
                            );

            coDescription = co.getDescription();
        }

        AiDraftQuestionsRequest aiRequest =
                new AiDraftQuestionsRequest(
                        request.subject(),
                        request.unit(),
                        coDescription,
                        request.bloomLevel().name(),
                        request.difficulty().name(),
                        request.marks(),
                        request.count(),
                        request.topicHint()
                );

        AiDraftQuestionsResponse aiResponse =
                aiServiceClient.draftQuestions(
                        aiRequest,
                        bearerToken
                );

        List<QuestionDraft> saved = new ArrayList<>();

        for (AiQuestionDraft d : aiResponse.drafts()) {

            QuestionDraft draft = new QuestionDraft();

            draft.setQuestionText(d.text());
            draft.setSubject(request.subject());
            draft.setUnit(request.unit());

            draft.setDifficulty(
                    parseDifficulty(
                            d.difficulty(),
                            request.difficulty()
                    )
            );

            draft.setBloomLevel(
                    parseBloomLevel(
                            d.bloomLevel(),
                            request.bloomLevel()
                    )
            );

            draft.setMarks(
                    d.marks() != null
                            ? d.marks()
                            : request.marks()
            );

            draft.setCourseOutcomeId(
                    request.courseOutcomeId()
            );

            draft.setConfidence(d.confidence());
            draft.setAiModel(aiResponse.model());
            draft.setStatus(DraftStatus.PENDING_REVIEW);
            draft.setCreatedBy(currentUser.username());
            draft.setCreatedAt(Instant.now());

            saved.add(
                    draftRepository.save(draft)
            );
        }

        return saved.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<QuestionDraftResponse> listByStatus(
            DraftStatus status) {

        DraftStatus effective =
                status == null
                        ? DraftStatus.PENDING_REVIEW
                        : status;

        return draftRepository
                .findByStatus(effective)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public QuestionResponse approve(
            Long draftId,
            DraftApprovalRequest overrides) {

        QuestionDraft draft = findDraft(draftId);

        if (draft.getStatus() != DraftStatus.PENDING_REVIEW) {

            throw new IllegalStateException(
                    "Draft "
                            + draftId
                            + " has already been "
                            + draft.getStatus()
            );
        }

        Set<Long> courseOutcomeIds =
                new HashSet<>();

        if (overrides.courseOutcomeIds() != null) {

            courseOutcomeIds.addAll(
                    overrides.courseOutcomeIds()
            );

        } else if (draft.getCourseOutcomeId() != null) {

            courseOutcomeIds.add(
                    draft.getCourseOutcomeId()
            );
        }

        /*
         * institutionId is required because
         * questions.institution_id is NOT NULL.
         *
         * Use the institution ID from the approval request.
         */
        QuestionRequest questionRequest =
                new QuestionRequest(

                        overrides.institutionId(),

                        overrides.subjectId(),

                        overrides.questionText() != null
                                ? overrides.questionText()
                                : draft.getQuestionText(),

                        draft.getSubject(),

                        draft.getUnit(),

                        overrides.difficulty() != null
                                ? overrides.difficulty()
                                : draft.getDifficulty(),

                        overrides.questionType() != null
                                ? overrides.questionType()
                                : com.questify.questionbank.entity.QuestionType.SHORT_ANSWER,

                        overrides.marks() != null
                                ? overrides.marks()
                                : draft.getMarks(),

                        overrides.bloomLevel() != null
                                ? overrides.bloomLevel()
                                : draft.getBloomLevel(),

                        courseOutcomeIds
                );

        QuestionResponse created =
                questionService.create(
                        questionRequest
                );

        draft.setStatus(
                DraftStatus.APPROVED
        );

        draft.setReviewedBy(
                currentUser.username()
        );

        draft.setReviewedAt(
                Instant.now()
        );

        draft.setApprovedQuestionId(
                created.id()
        );

        draftRepository.save(draft);

        return created;
    }

    @Transactional
    public void discard(Long draftId) {

        QuestionDraft draft =
                findDraft(draftId);

        if (draft.getStatus()
                != DraftStatus.PENDING_REVIEW) {

            throw new IllegalStateException(
                    "Draft "
                            + draftId
                            + " has already been "
                            + draft.getStatus()
            );
        }

        draft.setStatus(
                DraftStatus.DISCARDED
        );

        draft.setReviewedBy(
                currentUser.username()
        );

        draft.setReviewedAt(
                Instant.now()
        );

        draftRepository.save(draft);
    }

    private QuestionDraft findDraft(Long id) {

        return draftRepository
                .findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Question draft "
                                        + id
                                        + " was not found"
                        )
                );
    }

    /**
     * The model is instructed to echo back
     * the requested enum values, but never
     * trust that blindly.
     */
    private Difficulty parseDifficulty(
            String raw,
            Difficulty fallback) {

        if (raw == null) {
            return fallback;
        }

        try {

            return Difficulty.valueOf(
                    raw.trim().toUpperCase()
            );

        } catch (IllegalArgumentException ex) {

            return fallback;
        }
    }

    private BloomLevel parseBloomLevel(
            String raw,
            BloomLevel fallback) {

        if (raw == null) {
            return fallback;
        }

        try {

            return BloomLevel.valueOf(
                    raw.trim().toUpperCase()
            );

        } catch (IllegalArgumentException ex) {

            return fallback;
        }
    }

    private QuestionDraftResponse toResponse(
            QuestionDraft d) {

        return new QuestionDraftResponse(
                d.getId(),
                d.getQuestionText(),
                d.getSubject(),
                d.getUnit(),
                d.getDifficulty(),
                d.getMarks(),
                d.getBloomLevel(),
                d.getCourseOutcomeId(),
                d.getStatus(),
                d.getConfidence(),
                d.getAiModel(),
                d.getCreatedBy(),
                d.getCreatedAt(),
                d.getReviewedBy(),
                d.getReviewedAt(),
                d.getApprovedQuestionId()
        );
    }
}