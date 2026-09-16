package com.questify.questionbank.service;

import com.questify.questionbank.client.AiServiceClient;
import com.questify.questionbank.dto.DraftApprovalRequest;
import com.questify.questionbank.dto.DraftGenerationRequest;
import com.questify.questionbank.dto.QuestionDraftResponse;
import com.questify.questionbank.dto.QuestionResponse;
import com.questify.questionbank.dto.ai.AiDraftQuestionsResponse;
import com.questify.questionbank.dto.ai.AiQuestionDraft;
import com.questify.questionbank.entity.BloomLevel;
import com.questify.questionbank.entity.Difficulty;
import com.questify.questionbank.entity.DraftStatus;
import com.questify.questionbank.entity.QuestionDraft;
import com.questify.questionbank.entity.QuestionType;
import com.questify.questionbank.exception.ResourceNotFoundException;
import com.questify.questionbank.repository.CourseOutcomeRepository;
import com.questify.questionbank.repository.QuestionDraftRepository;
import com.questify.questionbank.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionDraftServiceTest {

    @Mock private QuestionDraftRepository draftRepository;
    @Mock private CourseOutcomeRepository courseOutcomeRepository;
    @Mock private AiServiceClient aiServiceClient;
    @Mock private QuestionService questionService;
    @Mock private CurrentUser currentUser;

    private QuestionDraftService service;

    @BeforeEach
    void setUp() {
        service = new QuestionDraftService(draftRepository, courseOutcomeRepository, aiServiceClient,
                questionService, currentUser);
        org.mockito.Mockito.lenient().when(currentUser.username()).thenReturn("faculty1");
    }

    private DraftGenerationRequest generationRequest() {
        return new DraftGenerationRequest("Data Structures", "Unit 3", null, BloomLevel.K4,
                Difficulty.MEDIUM, 5, 2, "Binary search trees");
    }

    @Test
    void generatePersistsOneDraftPerAiResult() {
        when(aiServiceClient.draftQuestions(any(), anyString())).thenReturn(new AiDraftQuestionsResponse(
                List.of(
                        new AiQuestionDraft("Compare traversal orders.", "K4", "MEDIUM", 5, 0.9),
                        new AiQuestionDraft("Analyze BST insertion complexity.", "K4", "MEDIUM", 5, 0.8)
                ),
                "claude-sonnet-5"));
        when(draftRepository.save(any(QuestionDraft.class))).thenAnswer(inv -> {
            QuestionDraft d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        List<QuestionDraftResponse> result = service.generate(generationRequest(), "Bearer token");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).status()).isEqualTo(DraftStatus.PENDING_REVIEW);
        assertThat(result.get(0).aiModel()).isEqualTo("claude-sonnet-5");
        assertThat(result.get(0).createdBy()).isEqualTo("faculty1");
    }

    @Test
    void approveCreatesRealQuestionAndMarksDraftApproved() {
        QuestionDraft draft = new QuestionDraft();
        draft.setId(5L);
        draft.setQuestionText("Compare traversal orders.");
        draft.setSubject("Data Structures");
        draft.setUnit("Unit 3");
        draft.setDifficulty(Difficulty.MEDIUM);
        draft.setBloomLevel(BloomLevel.K4);
        draft.setMarks(5);
        draft.setStatus(DraftStatus.PENDING_REVIEW);

        when(draftRepository.findById(5L)).thenReturn(Optional.of(draft));
        when(questionService.create(any())).thenReturn(new QuestionResponse(
                100L, "Compare traversal orders.", "Data Structures", "Unit 3",
                Difficulty.MEDIUM, QuestionType.SHORT_ANSWER, 5, BloomLevel.K4, List.of(),
                "faculty1", null, null));

        DraftApprovalRequest approval = new DraftApprovalRequest(null, null, null, null,
                QuestionType.SHORT_ANSWER, null);

        QuestionResponse response = service.approve(5L, approval);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(draft.getStatus()).isEqualTo(DraftStatus.APPROVED);
        assertThat(draft.getApprovedQuestionId()).isEqualTo(100L);
        assertThat(draft.getReviewedBy()).isEqualTo("faculty1");
    }

    @Test
    void approveRejectsAlreadyReviewedDraft() {
        QuestionDraft draft = new QuestionDraft();
        draft.setId(6L);
        draft.setStatus(DraftStatus.DISCARDED);
        when(draftRepository.findById(6L)).thenReturn(Optional.of(draft));

        DraftApprovalRequest approval = new DraftApprovalRequest(null, null, null, null,
                QuestionType.SHORT_ANSWER, null);

        assertThatThrownBy(() -> service.approve(6L, approval)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void discardMarksDraftDiscarded() {
        QuestionDraft draft = new QuestionDraft();
        draft.setId(7L);
        draft.setStatus(DraftStatus.PENDING_REVIEW);
        when(draftRepository.findById(7L)).thenReturn(Optional.of(draft));

        service.discard(7L);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.DISCARDED);
        assertThat(draft.getReviewedBy()).isEqualTo("faculty1");
    }

    @Test
    void approveThrowsWhenDraftMissing() {
        when(draftRepository.findById(99L)).thenReturn(Optional.empty());

        DraftApprovalRequest approval = new DraftApprovalRequest(null, null, null, null,
                QuestionType.SHORT_ANSWER, null);

        assertThatThrownBy(() -> service.approve(99L, approval)).isInstanceOf(ResourceNotFoundException.class);
    }
}
