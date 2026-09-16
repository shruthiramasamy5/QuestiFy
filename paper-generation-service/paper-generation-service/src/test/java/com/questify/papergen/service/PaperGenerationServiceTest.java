package com.questify.papergen.service;

import com.questify.papergen.ai.AiPaperGenerationProvider;
import com.questify.papergen.ai.AiProviderNotConfiguredException;
import com.questify.papergen.client.QuestionBankClient;
import com.questify.papergen.domain.GeneratedPaper;
import com.questify.papergen.dto.GeneratePaperRequest;
import com.questify.papergen.dto.QuestionDto;
import com.questify.papergen.exception.GenerationException;
import com.questify.papergen.repository.GeneratedPaperRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaperGenerationServiceTest {

    private QuestionBankClient client;
    private RepetitionCheckService repetition;
    private GeneratedPaperRepository repository;
    private AiPaperGenerationProvider aiProvider;
    private PaperGenerationService service;

    @BeforeEach
    void setUp() {
        client = mock(QuestionBankClient.class);
        repetition = mock(RepetitionCheckService.class);
        repository = mock(GeneratedPaperRepository.class);
        aiProvider = mock(AiPaperGenerationProvider.class);
        // Default: AI unavailable, so existing tests exercise the pure
        // rule-based path exactly as before this feature was added.
        when(aiProvider.isAvailable()).thenReturn(false);
        service = new PaperGenerationService(client, new QuestionSelectionService(), repetition, repository, aiProvider);
        when(repository.save(any(GeneratedPaper.class))).thenAnswer(inv -> {
            GeneratedPaper p = inv.getArgument(0);
            p.setId("generated-1");
            return p;
        });
    }

    private QuestionDto q(String id) {
        QuestionDto dto = new QuestionDto();
        dto.setId(id);
        dto.setText("Q" + id);
        dto.setSubjectCode("CS101");
        dto.setCourseOutcome("CO1");
        dto.setBloomLevel("APPLY");
        dto.setDifficulty("MEDIUM");
        dto.setMarks(5);
        return dto;
    }

    private GeneratePaperRequest request(int count) {
        GeneratePaperRequest r = new GeneratePaperRequest();
        r.setSubjectCode("CS101");
        r.setExamType("INTERNAL_1");
        r.setQuestionCount(count);
        r.setTotalMarks(50);
        r.setDurationMinutes(90);
        return r;
    }

    @Test
    void generatesAndPersistsDraft() {
        List<QuestionDto> pool = new ArrayList<>(List.of(q("1"), q("2"), q("3")));
        when(client.fetchCandidates(anyString(), any())).thenReturn(pool);
        when(repetition.previouslyUsedQuestionIds(anyString(), anyInt())).thenReturn(Set.of());

        GeneratedPaper paper = service.generate(request(2), "faculty1", "Bearer token");

        assertNotNull(paper.getId());
        assertEquals(2, paper.getQuestions().size());
        assertEquals("faculty1", paper.getCreatedBy());
        assertEquals(3, paper.getMetadata().getCandidatePoolSize());
    }

    @Test
    void excludesRepeatedQuestionsWhenPoolAllows() {
        List<QuestionDto> pool = new ArrayList<>(List.of(q("1"), q("2"), q("3"), q("4")));
        when(client.fetchCandidates(anyString(), any())).thenReturn(pool);
        when(repetition.previouslyUsedQuestionIds(anyString(), anyInt())).thenReturn(Set.of("1"));

        GeneratedPaper paper = service.generate(request(2), "faculty1", null);

        assertEquals(1, paper.getMetadata().getRepetitionExclusions());
        assertEquals(0, paper.getQuestions().stream().filter(x -> "1".equals(x.getQuestionId())).count());
    }

    @Test
    void rejectsDistributionLargerThanQuestionCount() {
        GeneratePaperRequest r = request(2);
        r.setCoDistribution(Map.of("CO1", 5));
        assertThrows(GenerationException.class, () -> service.generate(r, "faculty1", null));
    }

    @Test
    void usesAiSelectionWhenAvailableAndRecordsStrategy() {
        List<QuestionDto> pool = new ArrayList<>(List.of(q("1"), q("2"), q("3")));
        when(client.fetchCandidates(anyString(), any())).thenReturn(pool);
        when(repetition.previouslyUsedQuestionIds(anyString(), anyInt())).thenReturn(Set.of());
        when(aiProvider.isAvailable()).thenReturn(true);
        when(aiProvider.selectQuestions(any(GeneratePaperRequest.class), any()))
                .thenReturn(List.of(q("3"), q("1")));

        GeneratedPaper paper = service.generate(request(2), "faculty1", "Bearer token");

        assertEquals("AI_ASSISTED", paper.getMetadata().getStrategy());
        assertEquals(2, paper.getQuestions().size());
        assertEquals("3", paper.getQuestions().get(0).getQuestionId());
        assertEquals("1", paper.getQuestions().get(1).getQuestionId());
    }

    @Test
    void fallsBackToRuleBasedWhenAiFails() {
        List<QuestionDto> pool = new ArrayList<>(List.of(q("1"), q("2"), q("3")));
        when(client.fetchCandidates(anyString(), any())).thenReturn(pool);
        when(repetition.previouslyUsedQuestionIds(anyString(), anyInt())).thenReturn(Set.of());
        when(aiProvider.isAvailable()).thenReturn(true);
        when(aiProvider.selectQuestions(any(GeneratePaperRequest.class), any()))
                .thenThrow(new AiProviderNotConfiguredException("ai-service unreachable"));

        GeneratedPaper paper = service.generate(request(2), "faculty1", "Bearer token");

        assertEquals("RULE_BASED", paper.getMetadata().getStrategy());
        assertEquals(2, paper.getQuestions().size());
    }
}
