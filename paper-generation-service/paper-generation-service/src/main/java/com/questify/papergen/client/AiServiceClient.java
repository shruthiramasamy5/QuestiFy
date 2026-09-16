package com.questify.papergen.client;

import com.questify.papergen.exception.UpstreamServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Calls AI-SERVICE through Eureka service discovery. The host name is the
 * Eureka service id - never a hardcoded URL. Mirrors {@link QuestionBankClient}.
 */
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

    public <TRequest, TResponse> TResponse post(String path, TRequest body, Class<TResponse> responseType,
                                                 String bearerToken) {
        try {
            return webClient.post()
                    .uri(path)
                    .headers(h -> {
                        if (bearerToken != null && !bearerToken.isBlank()) {
                            h.set(HttpHeaders.AUTHORIZATION, bearerToken);
                        }
                    })
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block(Duration.ofSeconds(45));
        } catch (WebClientResponseException ex) {
            log.warn("ai-service responded {} for {}", ex.getStatusCode(), path);
            throw new UpstreamServiceException("AI-SERVICE returned " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            log.warn("ai-service call failed for {}", path, ex);
            throw new UpstreamServiceException("Unable to reach AI-SERVICE", ex);
        }
    }
}
