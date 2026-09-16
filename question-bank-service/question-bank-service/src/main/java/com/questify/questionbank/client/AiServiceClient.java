package com.questify.questionbank.client;

import com.questify.questionbank.dto.ai.AiDraftQuestionsRequest;
import com.questify.questionbank.dto.ai.AiDraftQuestionsResponse;
import com.questify.questionbank.exception.UpstreamServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/** Calls AI-SERVICE through Eureka service discovery, forwarding the caller's JWT. */
@Component
public class AiServiceClient {

    public static final String SERVICE_ID = "AI-SERVICE";

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);

    private final WebClient webClient;

    public AiServiceClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder
                .baseUrl("http://" + SERVICE_ID)
                .build();
    }

    public AiDraftQuestionsResponse draftQuestions(AiDraftQuestionsRequest request, String bearerToken) {
        try {
            return webClient.post()
                    .uri("/api/ai/draft-questions")
                    .headers(h -> {
                        if (bearerToken != null && !bearerToken.isBlank()) {
                            h.set(HttpHeaders.AUTHORIZATION, bearerToken);
                        }
                    })
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiDraftQuestionsResponse.class)
                    .block(Duration.ofSeconds(45));
        } catch (WebClientResponseException ex) {
            log.warn("ai-service responded {} for draft-questions", ex.getStatusCode());
            throw new UpstreamServiceException("AI-SERVICE returned " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            log.warn("ai-service call failed for draft-questions", ex);
            throw new UpstreamServiceException("Unable to reach AI-SERVICE", ex);
        }
    }
}
