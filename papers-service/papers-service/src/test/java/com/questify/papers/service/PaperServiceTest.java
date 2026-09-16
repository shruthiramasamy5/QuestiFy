package com.questify.papers.service;

import com.questify.papers.client.PaperGenerationClient;
import com.questify.papers.dto.CreatePaperFromDraftRequest;
import com.questify.papers.dto.CreatePaperRequest;
import com.questify.papers.dto.EvaluationSchemeRequest;
import com.questify.papers.dto.GeneratedDraftDto;
import com.questify.papers.entity.EvaluationScheme;
import com.questify.papers.entity.Paper;
import com.questify.papers.entity.PaperStatus;
import com.questify.papers.exception.BusinessRuleException;
import com.questify.papers.exception.ResourceNotFoundException;
import com.questify.papers.repository.EvaluationSchemeRepository;
import com.questify.papers.repository.PaperRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaperServiceTest {

    private PaperRepository paperRepository;
    private EvaluationSchemeRepository schemeRepository;
    private PaperGenerationClient client;
    private PaperService service;

    @BeforeEach
    void setUp() {
        paperRepository = mock(PaperRepository.class);
        schemeRepository = mock(EvaluationSchemeRepository.class);
        client = mock(PaperGenerationClient.class);
        service = new PaperService(paperRepository, schemeRepository, client);
        when(paperRepository.save(any(Paper.class))).thenAnswer(inv -> {
            Paper p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId(1L);
            }
            return p;
        });
        when(schemeRepository.save(any(EvaluationScheme.class))).thenAnswer(inv -> {
            EvaluationScheme s = inv.getArgument(0);
            if (s.getId() == null) {
                s.setId(10L);
            }
            return s;
        });
    }

    private CreatePaperRequest createRequest() {
        CreatePaperRequest r = new CreatePaperRequest();
        r.setTitle("CS101 Internal 1");
        r.setSubjectCode("CS101");
        r.setExamType("INTERNAL_1");
        r.setTotalMarks(50);
        r.setDurationMinutes(90);
        return r;
    }

    @Test
    void createsPaper() {
        Paper paper = service.create(createRequest(), "faculty1");
        assertEquals(1L, paper.getId());
        assertEquals(PaperStatus.DRAFT, paper.getStatus());
        assertEquals("faculty1", paper.getCreatedBy());
    }

    @Test
    void rejectsDuplicateDraftReference() {
        CreatePaperRequest r = createRequest();
        r.setGeneratedDraftId("draft-1");
        when(paperRepository.existsByGeneratedDraftId("draft-1")).thenReturn(true);
        assertThrows(BusinessRuleException.class, () -> service.create(r, "faculty1"));
    }

    @Test
    void createsPaperFromDraftAndSeedsRubrics() {
        GeneratedDraftDto draft = new GeneratedDraftDto();
        draft.setId("draft-9");
        draft.setSubjectCode("CS101");
        draft.setSubjectName("Data Structures");
        draft.setExamType("INTERNAL_1");
        draft.setTotalMarks(50);
        draft.setDurationMinutes(90);
        GeneratedDraftDto.DraftQuestion q = new GeneratedDraftDto.DraftQuestion();
        q.setQuestionId("q1");
        q.setMarks(50);
        q.setSequence(1);
        draft.setQuestions(List.of(q));
        when(client.fetchDraft(anyString(), any())).thenReturn(draft);

        CreatePaperFromDraftRequest req = new CreatePaperFromDraftRequest();
        req.setDraftId("draft-9");

        Paper paper = service.createFromDraft(req, "faculty1", "Bearer x");

        assertEquals("draft-9", paper.getGeneratedDraftId());
        assertEquals("CS101 - INTERNAL_1", paper.getTitle());
        assertNotNull(paper.getEvaluationScheme());
        assertEquals(1, paper.getEvaluationScheme().getRubrics().size());
    }

    @Test
    void throwsWhenPaperMissing() {
        when(paperRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void savesEvaluationSchemeWhenMarksBalance() {
        Paper paper = service.create(createRequest(), "faculty1");
        when(paperRepository.findById(1L)).thenReturn(Optional.of(paper));
        when(schemeRepository.findByPaperId(1L)).thenReturn(Optional.empty());

        EvaluationSchemeRequest req = new EvaluationSchemeRequest();
        req.setTotalMarks(50);
        req.setPassingMarks(20);
        EvaluationSchemeRequest.RubricRequest r1 = new EvaluationSchemeRequest.RubricRequest();
        r1.setQuestionNumber(1);
        r1.setMarks(20);
        EvaluationSchemeRequest.RubricRequest r2 = new EvaluationSchemeRequest.RubricRequest();
        r2.setQuestionNumber(2);
        r2.setMarks(30);
        req.setRubrics(List.of(r1, r2));

        EvaluationScheme saved = service.upsertEvaluationScheme(1L, req, "hod1");
        assertEquals(50, saved.getTotalMarks());
        assertEquals(2, saved.getRubrics().size());
    }

    @Test
    void rejectsRubricTotalMismatch() {
        Paper paper = service.create(createRequest(), "faculty1");
        when(paperRepository.findById(1L)).thenReturn(Optional.of(paper));

        EvaluationSchemeRequest req = new EvaluationSchemeRequest();
        req.setTotalMarks(50);
        EvaluationSchemeRequest.RubricRequest r1 = new EvaluationSchemeRequest.RubricRequest();
        r1.setQuestionNumber(1);
        r1.setMarks(10);
        req.setRubrics(List.of(r1));

        assertThrows(BusinessRuleException.class, () -> service.upsertEvaluationScheme(1L, req, "hod1"));
    }

    @Test
    void rejectsSchemeTotalNotMatchingPaper() {
        Paper paper = service.create(createRequest(), "faculty1");
        when(paperRepository.findById(1L)).thenReturn(Optional.of(paper));

        EvaluationSchemeRequest req = new EvaluationSchemeRequest();
        req.setTotalMarks(100);
        assertThrows(BusinessRuleException.class, () -> service.upsertEvaluationScheme(1L, req, "hod1"));
    }
}
