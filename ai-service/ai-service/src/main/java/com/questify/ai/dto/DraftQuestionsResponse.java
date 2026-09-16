package com.questify.ai.dto;

import java.util.List;

public record DraftQuestionsResponse(List<QuestionDraftDto> drafts, String model) {
}
