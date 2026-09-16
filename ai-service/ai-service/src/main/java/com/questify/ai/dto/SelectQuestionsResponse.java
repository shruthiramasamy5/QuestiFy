package com.questify.ai.dto;

import java.util.List;

public record SelectQuestionsResponse(List<String> selectedQuestionIds, String reasoning) {
}
