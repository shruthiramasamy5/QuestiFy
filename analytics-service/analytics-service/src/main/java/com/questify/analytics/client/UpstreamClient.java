package com.questify.analytics.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.questify.analytics.config.AnalyticsProperties;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Reads data from the other QuestiFy services through Eureka service discovery.
 * A failing upstream degrades gracefully to an empty list so a single outage does
 * not break the whole dashboard.
 */
@Component
public class UpstreamClient {

    private static final Logger log = LoggerFactory.getLogger(UpstreamClient.class);

    private final RestClient restClient;
    private final AnalyticsProperties properties;

    public UpstreamClient(RestClient restClient, AnalyticsProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuestionPage {
        private List<RemoteQuestion> content = new ArrayList<>();
        public List<RemoteQuestion> getContent() { return content; }
        public void setContent(List<RemoteQuestion> content) { this.content = content; }
    }

    public List<RemoteQuestion> questions(String bearerToken) {
        try {
            QuestionPage page = restClient.get()
                    .uri("http://" + properties.getQuestionBankService() + "/api/questions?size=500")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .retrieve()
                    .body(QuestionPage.class);
            return page != null && page.getContent() != null ? page.getContent() : List.of();
        } catch (RuntimeException ex) {
            log.warn("Upstream {}/api/questions unavailable: {}", properties.getQuestionBankService(), ex.getMessage());
            return List.of();
        }
    }

    public List<RemotePaper> papers(String bearerToken) {
        return get(properties.getPapersService(), "/api/papers", bearerToken,
                new ParameterizedTypeReference<List<RemotePaper>>() {
                });
    }

    public List<RemotePaper> generatedPapers(String bearerToken) {
        return get(properties.getPaperGenerationService(), "/api/generate", bearerToken,
                new ParameterizedTypeReference<List<RemotePaper>>() {
                });
    }

    public List<RemoteApproval> approvals(String bearerToken) {
        return get(properties.getApprovalService(), "/api/approvals", bearerToken,
                new ParameterizedTypeReference<List<RemoteApproval>>() {
                });
    }

    private <T> List<T> get(String serviceId, String path, String bearerToken,
                            ParameterizedTypeReference<List<T>> type) {
        try {
            List<T> body = restClient.get()
                    .uri("http://" + serviceId + path)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .retrieve()
                    .body(type);
            return body == null ? List.of() : body;
        } catch (RuntimeException ex) {
            log.warn("Upstream {}{} unavailable: {}", serviceId, path, ex.getMessage());
            return List.of();
        }
    }
}
