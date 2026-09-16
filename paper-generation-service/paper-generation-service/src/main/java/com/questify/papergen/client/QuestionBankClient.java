package com.questify.papergen.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.questify.papergen.dto.QuestionDto;
import com.questify.papergen.exception.UpstreamServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Calls QUESTION-BANK-SERVICE through Eureka service discovery.
 * The host name is the Eureka service id - never a hardcoded URL.
 */
@Component
public class QuestionBankClient {

    public static final String SERVICE_ID = "QUESTION-BANK-SERVICE";

    private static final Logger log = LoggerFactory.getLogger(QuestionBankClient.class);

    private final WebClient webClient;

    public QuestionBankClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder
                .baseUrl("http://" + SERVICE_ID)
                .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuestionBankPage {
        private List<QuestionDto> content = new ArrayList<>();
        private int totalElements;

        public List<QuestionDto> getContent() { return content; }
        public void setContent(List<QuestionDto> content) { this.content = content; }
        public int getTotalElements() { return totalElements; }
        public void setTotalElements(int totalElements) { this.totalElements = totalElements; }
    }

    /** Fetch candidate questions for a subject, propagating the caller's JWT. */
    public List<QuestionDto> fetchCandidates(String subjectCode, String bearerToken) {
        try {
            QuestionBankPage page = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/questions")
                            .queryParam("subject", subjectCode)
                            .queryParam("size", 500)
                            .build())
                    .headers(h -> {
                        if (bearerToken != null && !bearerToken.isBlank()) {
                            h.set(HttpHeaders.AUTHORIZATION, bearerToken);
                        }
                    })
                    .retrieve()
                    .bodyToMono(QuestionBankPage.class)
                    .block(Duration.ofSeconds(15));

            return page != null && page.getContent() != null ? page.getContent() : List.of();
        } catch (WebClientResponseException ex) {
            log.error("question-bank-service responded {}", ex.getStatusCode(), ex);
            throw new UpstreamServiceException(
                    "QUESTION-BANK-SERVICE returned " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            log.error("question-bank-service call failed", ex);
            throw new UpstreamServiceException("Unable to reach QUESTION-BANK-SERVICE", ex);
        }
    }
}
