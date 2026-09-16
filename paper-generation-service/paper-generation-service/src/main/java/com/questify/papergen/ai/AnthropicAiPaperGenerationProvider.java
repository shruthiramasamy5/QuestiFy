package com.questify.papergen.ai;

import com.questify.papergen.client.AiServiceClient;
import com.questify.papergen.dto.GeneratePaperRequest;
import com.questify.papergen.dto.QuestionDto;
import com.questify.papergen.dto.ai.AiCandidateQuestion;
import com.questify.papergen.dto.ai.AiSelectQuestionsRequest;
import com.questify.papergen.dto.ai.AiSelectQuestionsResponse;
import com.questify.papergen.exception.UpstreamServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calls ai-service (which wraps the real Anthropic API) to re-rank/select
 * questions for a paper. Marked {@code @Primary} so it is the bean used
 * wherever {@link AiPaperGenerationProvider} is injected; unlike
 * {@link NoopAiPaperGenerationProvider} it is genuinely capable of
 * returning an AI-assisted selection when ai-service is reachable and
 * configured with an Anthropic API key - and degrades to "unavailable"
 * (never a fake result) when it is not.
 */
@Component
@Primary
public class AnthropicAiPaperGenerationProvider implements AiPaperGenerationProvider {

    private static final Logger log = LoggerFactory.getLogger(AnthropicAiPaperGenerationProvider.class);

    private final AiServiceClient aiServiceClient;
    private final boolean enabled;

    public AnthropicAiPaperGenerationProvider(AiServiceClient aiServiceClient,
                                               @Value("${questify.ai.enabled:true}") boolean enabled) {
        this.aiServiceClient = aiServiceClient;
        this.enabled = enabled;
    }

    /**
     * Whether AI selection should even be attempted. This is a feature
     * flag, not a live health check - actual availability (is an API key
     * configured?) is determined by ai-service at call time, and a "not
     * configured" response there is treated the same as any other
     * failure: caller falls back to rule-based selection.
     */
    @Override
    public boolean isAvailable() {
        return enabled;
    }

    @Override
    public List<QuestionDto> selectQuestions(GeneratePaperRequest request, List<QuestionDto> candidates) {
        String bearerToken = currentBearerToken();

        AiSelectQuestionsRequest aiRequest = new AiSelectQuestionsRequest(
                request.getQuestionCount(),
                request.getTotalMarks(),
                request.getCoDistribution(),
                request.getBloomDistribution(),
                request.getDifficultyMix(),
                candidates.stream()
                        .map(q -> new AiCandidateQuestion(q.getId(), q.getText(), q.getCourseOutcome(),
                                q.getBloomLevel(), q.getDifficulty(), q.getMarks()))
                        .toList()
        );

        AiSelectQuestionsResponse response;
        try {
            response = aiServiceClient.post("/api/ai/select-questions", aiRequest,
                    AiSelectQuestionsResponse.class, bearerToken);
        } catch (UpstreamServiceException ex) {
            // ai-service unreachable, misconfigured, or rejected the request
            // (including "AI provider not configured", which arrives as a
            // 503 from ai-service) - surface as AiProviderNotConfiguredException
            // so PaperGenerationService's existing fallback path handles it.
            throw new AiProviderNotConfiguredException("AI selection unavailable: " + ex.getMessage());
        }

        if (response == null || response.selectedQuestionIds() == null) {
            throw new AiProviderNotConfiguredException("AI selection returned no result");
        }

        Map<String, QuestionDto> byId = new LinkedHashMap<>();
        for (QuestionDto q : candidates) {
            byId.put(q.getId(), q);
        }

        return response.selectedQuestionIds().stream()
                .map(id -> {
                    QuestionDto q = byId.get(id);
                    if (q == null) {
                        // Defense in depth: ai-service already validates this, but
                        // this service never trusts an id it didn't hand out itself.
                        throw new AiProviderNotConfiguredException(
                                "AI selected an unknown question id: " + id);
                    }
                    return q;
                })
                .toList();
    }

    private String currentBearerToken() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        String header = attrs.getRequest().getHeader("Authorization");
        if (header == null) {
            log.debug("No Authorization header on current request; calling ai-service without one");
        }
        return header;
    }
}
