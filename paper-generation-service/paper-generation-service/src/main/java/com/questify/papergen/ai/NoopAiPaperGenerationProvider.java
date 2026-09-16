package com.questify.papergen.ai;

import com.questify.papergen.dto.GeneratePaperRequest;
import com.questify.papergen.dto.QuestionDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Default provider used while no AI integration exists.
 * It reports itself as unavailable and refuses to pretend to generate.
 */
@Component
public class NoopAiPaperGenerationProvider implements AiPaperGenerationProvider {

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public List<QuestionDto> selectQuestions(GeneratePaperRequest request, List<QuestionDto> candidates) {
        throw new AiProviderNotConfiguredException(
                "No AI provider is configured. Paper generation uses the deterministic rule-based selector.");
    }
}
