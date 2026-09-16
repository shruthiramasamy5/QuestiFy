package com.questify.ai.controller;

import com.questify.ai.dto.DraftQuestionsRequest;
import com.questify.ai.dto.DraftQuestionsResponse;
import com.questify.ai.dto.SelectQuestionsRequest;
import com.questify.ai.dto.SelectQuestionsResponse;
import com.questify.ai.service.QuestionDraftAiService;
import com.questify.ai.service.QuestionSelectionAiService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD')")
public class AiController {

    private final QuestionSelectionAiService selectionService;
    private final QuestionDraftAiService draftService;

    public AiController(QuestionSelectionAiService selectionService, QuestionDraftAiService draftService) {
        this.selectionService = selectionService;
        this.draftService = draftService;
    }

    /** Called by paper-generation-service to re-rank/select from an already-filtered candidate pool. */
    @PostMapping("/select-questions")
    public SelectQuestionsResponse selectQuestions(@Valid @RequestBody SelectQuestionsRequest request) {
        return selectionService.select(request);
    }

    /** Called by question-bank-service to draft new questions for the faculty review queue. */
    @PostMapping("/draft-questions")
    public DraftQuestionsResponse draftQuestions(@Valid @RequestBody DraftQuestionsRequest request) {
        return draftService.draft(request);
    }
}
