package com.questify.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.questify.ai.config.GeminiProperties;
import com.questify.ai.exception.AiProviderNotConfiguredException;
import com.questify.ai.exception.AiUpstreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private static final Logger log =
            LoggerFactory.getLogger(GeminiClient.class);

    private final RestClient restClient;
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;

    public GeminiClient(
            RestClient geminiRestClient,
            GeminiProperties properties,
            ObjectMapper objectMapper) {

        this.restClient = geminiRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public boolean isAvailable() {
        return properties.isConfigured();
    }

    public String sendMessage(
            String systemPrompt,
            String userPrompt) {

        if (!properties.isConfigured()) {
            throw new AiProviderNotConfiguredException(
                    "GEMINI_API_KEY is not set - AI features are unavailable until it is configured"
            );
        }

        String fullPrompt =
                systemPrompt
                        + "\n\n"
                        + userPrompt;

        Map<String, Object> requestBody =
                Map.of(
                        "contents",
                        List.of(
                                Map.of(
                                        "parts",
                                        List.of(
                                                Map.of(
                                                        "text",
                                                        fullPrompt
                                                )
                                        )
                                )
                        ),
                        "generationConfig",
                        Map.of(
                                "maxOutputTokens",
                                2048
                        )
                );

        try {

            String responseBody =
                    restClient.post()
                            .uri(
                                    "/v1beta/models/"
                                            + properties.getModel()
                                            + ":generateContent?key="
                                            + properties.getApiKey()
                            )
                            .body(requestBody)
                            .retrieve()
                            .body(String.class);

            return extractText(responseBody);

        } catch (RestClientException ex) {

            log.error("Gemini API call failed", ex);

            throw new AiUpstreamException(
                    "Gemini API call failed: "
                            + ex.getMessage(),
                    ex
            );
        }
    }

    private String extractText(String responseBody) {

        try {

            JsonNode root =
                    objectMapper.readTree(responseBody);

            JsonNode candidates =
                    root.path("candidates");

            if (!candidates.isArray()
                    || candidates.isEmpty()) {

                throw new AiUpstreamException(
                        "Gemini response had no candidates: "
                                + responseBody
                );
            }

            JsonNode parts =
                    candidates
                            .get(0)
                            .path("content")
                            .path("parts");

            if (!parts.isArray()
                    || parts.isEmpty()) {

                throw new AiUpstreamException(
                        "Gemini response had no content: "
                                + responseBody
                );
            }

            StringBuilder text =
                    new StringBuilder();

            for (JsonNode part : parts) {

                JsonNode textNode =
                        part.path("text");

                if (!textNode.isMissingNode()) {
                    text.append(
                            textNode.asText()
                    );
                }
            }

            if (text.isEmpty()) {

                throw new AiUpstreamException(
                        "Gemini response had no text content: "
                                + responseBody
                );
            }

            return text.toString();

        } catch (AiUpstreamException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new AiUpstreamException(
                    "Could not parse Gemini response: "
                            + ex.getMessage(),
                    ex
            );
        }
    }
}