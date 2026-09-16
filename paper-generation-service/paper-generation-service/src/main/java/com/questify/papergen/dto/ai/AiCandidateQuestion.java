package com.questify.papergen.dto.ai;

/** Mirrors ai-service's CandidateQuestionDto - this service's own view of the wire contract. */
public record AiCandidateQuestion(String id, String text, String courseOutcome, String bloomLevel,
                                   String difficulty, Integer marks) {
}
