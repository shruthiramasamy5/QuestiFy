package com.questify.papergen.ai;

import com.questify.papergen.dto.GeneratePaperRequest;
import com.questify.papergen.dto.QuestionDto;

import java.util.List;

/**
 * Extension point for AI-assisted paper generation.
 *
 * NOTE: No AI provider is wired up yet. This interface exists so a real
 * provider (LLM based ranking / question synthesis) can be plugged in later
 * without touching the generation pipeline. There is deliberately no fake or
 * simulated implementation in this service.
 */
public interface AiPaperGenerationProvider {

    /** @return true when a real provider is configured and usable. */
    boolean isAvailable();

    /**
     * Re-rank or select questions using AI.
     *
     * @throws AiProviderNotConfiguredException when no provider is configured.
     */
    List<QuestionDto> selectQuestions(GeneratePaperRequest request, List<QuestionDto> candidates);
}
