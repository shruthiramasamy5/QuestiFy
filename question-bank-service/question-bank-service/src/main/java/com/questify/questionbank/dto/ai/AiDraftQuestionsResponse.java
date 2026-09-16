package com.questify.questionbank.dto.ai;

import java.util.List;

public record AiDraftQuestionsResponse(List<AiQuestionDraft> drafts, String model) {
}
