package com.questify.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.questify.ai.client.GeminiClient;
import com.questify.ai.config.GeminiProperties;
import com.questify.ai.dto.DraftQuestionsRequest;
import com.questify.ai.dto.DraftQuestionsResponse;
import com.questify.ai.exception.AiResponseValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionDraftAiServiceTest {

    @Mock
    private GeminiClient geminiClient;

    private QuestionDraftAiService service;

    private DraftQuestionsRequest request;

    @BeforeEach
    void setUp() {

        GeminiProperties properties = new GeminiProperties();

        properties.setModel("gemini-2.5-flash");

        service = new QuestionDraftAiService(
                geminiClient,
                properties,
                new ObjectMapper()
        );

        request = new DraftQuestionsRequest(
                "Data Structures",
                "Unit 3",
                "Analyze tree traversal algorithms",
                "K4",
                "MEDIUM",
                5,
                2,
                "Binary search trees"
        );
    }

    @Test
    void acceptsWellFormedDrafts() {

        when(geminiClient.sendMessage(anyString(), anyString()))
                .thenReturn("""
                {
                  "drafts": [
                    {
                      "text": "Compare in-order and pre-order traversal of a BST.",
                      "bloomLevel": "K4",
                      "difficulty": "MEDIUM",
                      "marks": 5,
                      "confidence": 0.85
                    },
                    {
                      "text": "Analyze the time complexity of BST insertion in the worst case.",
                      "bloomLevel": "K4",
                      "difficulty": "MEDIUM",
                      "marks": 5,
                      "confidence": 0.8
                    }
                  ]
                }
                """);

        DraftQuestionsResponse response =
                service.draft(request);

        assertThat(response.drafts())
                .hasSize(2);

        assertThat(response.model())
                .isEqualTo("gemini-2.5-flash");

        assertThat(response.drafts()
                .get(0)
                .confidence())
                .isEqualTo(0.85);
    }

    @Test
    void rejectsEmptyDraftList() {

        when(geminiClient.sendMessage(anyString(), anyString()))
                .thenReturn("""
                {"drafts": []}
                """);

        assertThatThrownBy(() ->
                service.draft(request)
        )
        .isInstanceOf(
                AiResponseValidationException.class
        );
    }

    @Test
    void rejectsDraftWithBlankText() {

        when(geminiClient.sendMessage(anyString(), anyString()))
                .thenReturn("""
                {
                  "drafts": [
                    {
                      "text": "",
                      "bloomLevel": "K4",
                      "difficulty": "MEDIUM",
                      "marks": 5
                    }
                  ]
                }
                """);

        assertThatThrownBy(() ->
                service.draft(request)
        )
        .isInstanceOf(
                AiResponseValidationException.class
        );
    }

    @Test
    void rejectsMoreDraftsThanRequested() {

        when(geminiClient.sendMessage(anyString(), anyString()))
                .thenReturn("""
                {
                  "drafts": [
                    {
                      "text": "Q1",
                      "bloomLevel": "K4",
                      "difficulty": "MEDIUM",
                      "marks": 5
                    },
                    {
                      "text": "Q2",
                      "bloomLevel": "K4",
                      "difficulty": "MEDIUM",
                      "marks": 5
                    },
                    {
                      "text": "Q3",
                      "bloomLevel": "K4",
                      "difficulty": "MEDIUM",
                      "marks": 5
                    }
                  ]
                }
                """);

        assertThatThrownBy(() ->
                service.draft(request)
        )
        .isInstanceOf(
                AiResponseValidationException.class
        );
    }
}