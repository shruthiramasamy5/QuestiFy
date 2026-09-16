package com.questify.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.questify.ai.client.GeminiClient;
import com.questify.ai.dto.CandidateQuestionDto;
import com.questify.ai.dto.SelectQuestionsRequest;
import com.questify.ai.dto.SelectQuestionsResponse;
import com.questify.ai.exception.AiResponseValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionSelectionAiServiceTest {

    @Mock
    private GeminiClient geminiClient;

    private QuestionSelectionAiService service;

    private SelectQuestionsRequest request;

    @BeforeEach
    void setUp() {

        service = new QuestionSelectionAiService(
                geminiClient,
                new ObjectMapper()
        );

        request = new SelectQuestionsRequest(
                2,
                10,

                Map.of(
                        "CO1", 1,
                        "CO2", 1
                ),

                Map.of(
                        "K2", 1,
                        "K3", 1
                ),

                Map.of(
                        "MEDIUM", 2
                ),

                List.of(
                        new CandidateQuestionDto(
                                "q1",
                                "Explain X",
                                "CO1",
                                "K2",
                                "MEDIUM",
                                5
                        ),

                        new CandidateQuestionDto(
                                "q2",
                                "Apply Y to solve Z",
                                "CO2",
                                "K3",
                                "MEDIUM",
                                5
                        ),

                        new CandidateQuestionDto(
                                "q3",
                                "Define W",
                                "CO1",
                                "K1",
                                "EASY",
                                5
                        )
                )
        );
    }

    @Test
    void acceptsWellFormedResponseWithinCandidatePool() {

        when(geminiClient.sendMessage(
                anyString(),
                anyString()
        )).thenReturn(
                """
                {
                  "selectedQuestionIds": ["q1", "q2"],
                  "reasoning": "Best CO/Bloom coverage"
                }
                """
        );

        SelectQuestionsResponse response =
                service.select(request);

        assertThat(response.selectedQuestionIds())
                .containsExactly("q1", "q2");

        assertThat(response.reasoning())
                .isNotBlank();
    }

    @Test
    void toleratesProseWrappedAroundJson() {

        when(geminiClient.sendMessage(
                anyString(),
                anyString()
        )).thenReturn(
                """
                Sure, here is my selection:
                {"selectedQuestionIds": ["q1", "q2"], "reasoning": "ok"}
                Let me know if you need changes.
                """
        );

        SelectQuestionsResponse response =
                service.select(request);

        assertThat(response.selectedQuestionIds())
                .containsExactly("q1", "q2");
    }

    @Test
    void rejectsIdNotInCandidatePool() {

        when(geminiClient.sendMessage(
                anyString(),
                anyString()
        )).thenReturn(
                """
                {
                  "selectedQuestionIds": ["q1", "q99"],
                  "reasoning": "ok"
                }
                """
        );

        assertThatThrownBy(() ->
                service.select(request)
        )
        .isInstanceOf(
                AiResponseValidationException.class
        )
        .hasMessageContaining("q99");
    }

    @Test
    void rejectsWrongCount() {

        when(geminiClient.sendMessage(
                anyString(),
                anyString()
        )).thenReturn(
                """
                {
                  "selectedQuestionIds": ["q1"],
                  "reasoning": "ok"
                }
                """
        );

        assertThatThrownBy(() ->
                service.select(request)
        )
        .isInstanceOf(
                AiResponseValidationException.class
        );
    }

    @Test
    void rejectsDuplicateIds() {

        when(geminiClient.sendMessage(
                anyString(),
                anyString()
        )).thenReturn(
                """
                {
                  "selectedQuestionIds": ["q1", "q1"],
                  "reasoning": "ok"
                }
                """
        );

        assertThatThrownBy(() ->
                service.select(request)
        )
        .isInstanceOf(
                AiResponseValidationException.class
        );
    }

    @Test
    void rejectsGarbageResponse() {

        when(geminiClient.sendMessage(
                anyString(),
                anyString()
        )).thenReturn(
                "not json at all"
        );

        assertThatThrownBy(() ->
                service.select(request)
        )
        .isInstanceOf(
                AiResponseValidationException.class
        );
    }
}