package com.questify.papergen.dto.ai;

import java.util.List;

public record AiSelectQuestionsResponse(List<String> selectedQuestionIds, String reasoning) {
}
