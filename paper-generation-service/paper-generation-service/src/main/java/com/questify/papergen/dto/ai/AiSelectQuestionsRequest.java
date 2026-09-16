package com.questify.papergen.dto.ai;

import java.util.List;
import java.util.Map;

public record AiSelectQuestionsRequest(Integer questionCount, Integer totalMarks,
                                        Map<String, Integer> coDistribution,
                                        Map<String, Integer> bloomDistribution,
                                        Map<String, Integer> difficultyMix,
                                        List<AiCandidateQuestion> candidates) {
}
