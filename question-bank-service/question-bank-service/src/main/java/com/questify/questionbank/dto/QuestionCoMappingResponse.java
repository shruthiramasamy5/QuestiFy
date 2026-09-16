package com.questify.questionbank.dto;

import java.util.List;

public record QuestionCoMappingResponse(
        Long questionId,
        String questionText,
        List<CourseOutcomeResponse> courseOutcomes) {
}
