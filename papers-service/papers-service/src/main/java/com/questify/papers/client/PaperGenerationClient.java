package com.questify.papers.client;

import com.questify.papers.dto.GeneratedDraftDto;
import com.questify.papers.exception.UpstreamServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/** Reads generated drafts over HTTP via Eureka - never from the other service's database. */
@Component
public class PaperGenerationClient {

    public static final String SERVICE_ID = "PAPER-GENERATION-SERVICE";

    private static final Logger log = LoggerFactory.getLogger(PaperGenerationClient.class);

    private final WebClient webClient;

    public PaperGenerationClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder.baseUrl("http://" + SERVICE_ID).build();
    }

    public GeneratedDraftDto fetchDraft(String draftId, String bearerToken) {
        try {
            return webClient.get()
                    .uri("/api/generate/{id}", draftId)
                    .headers(h -> {
                        if (bearerToken != null && !bearerToken.isBlank()) {
                            h.set(HttpHeaders.AUTHORIZATION, bearerToken);
                        }
                    })
                    .retrieve()
                    .bodyToMono(GeneratedDraftDto.class)
                    .block(Duration.ofSeconds(15));
        } catch (WebClientResponseException.NotFound ex) {
            throw new UpstreamServiceException("Draft " + draftId + " was not found in " + SERVICE_ID, ex);
        } catch (Exception ex) {
            log.error("Failed to fetch draft {}", draftId, ex);
            throw new UpstreamServiceException("Unable to reach " + SERVICE_ID, ex);
        }
    }
}
