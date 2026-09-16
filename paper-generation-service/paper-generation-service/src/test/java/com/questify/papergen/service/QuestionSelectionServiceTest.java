package com.questify.papergen.service;

import com.questify.papergen.domain.GeneratedQuestion;
import com.questify.papergen.dto.GeneratePaperRequest;
import com.questify.papergen.dto.QuestionDto;
import com.questify.papergen.exception.GenerationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionSelectionServiceTest {

    private final QuestionSelectionService service = new QuestionSelectionService();

    private QuestionDto q(String id, String co, String bloom, String difficulty, int marks) {
        QuestionDto dto = new QuestionDto();
        dto.setId(id);
        dto.setText("Question " + id);
        dto.setSubjectCode("CS101");
        dto.setCourseOutcome(co);
        dto.setBloomLevel(bloom);
        dto.setDifficulty(difficulty);
        dto.setMarks(marks);
        return dto;
    }

    private List<QuestionDto> pool() {
        List<QuestionDto> pool = new ArrayList<>();
        pool.add(q("1", "CO1", "REMEMBER", "EASY", 2));
        pool.add(q("2", "CO1", "UNDERSTAND", "EASY", 2));
        pool.add(q("3", "CO2", "APPLY", "MEDIUM", 5));
        pool.add(q("4", "CO2", "ANALYZE", "HARD", 10));
        pool.add(q("5", "CO3", "APPLY", "MEDIUM", 5));
        pool.add(q("6", "CO3", "CREATE", "HARD", 10));
        return pool;
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
    void selectsRequestedNumberOfQuestions() {
        List<GeneratedQuestion> selected = service.select(request(4), pool());
        assertEquals(4, selected.size());
        assertEquals(1, selected.get(0).getSequence());
    }

    @Test
    void honoursCoDistribution() {
        GeneratePaperRequest r = request(3);
        r.setCoDistribution(Map.of("CO2", 2));
        List<GeneratedQuestion> selected = service.select(r, pool());
        long co2 = selected.stream().filter(x -> "CO2".equals(x.getCourseOutcome())).count();
        assertTrue(co2 >= 2, "expected at least two CO2 questions");
    }

    @Test
    void honoursDifficultyMix() {
        GeneratePaperRequest r = request(3);
        r.setDifficultyMix(Map.of("HARD", 2));
        List<GeneratedQuestion> selected = service.select(r, pool());
        long hard = selected.stream().filter(x -> "HARD".equals(x.getDifficulty())).count();
        assertTrue(hard >= 2);
    }

    @Test
    void failsWhenPoolIsTooSmall() {
        assertThrows(GenerationException.class, () -> service.select(request(20), pool()));
    }

    @Test
    void failsWhenPoolIsEmpty() {
        assertThrows(GenerationException.class, () -> service.select(request(2), List.of()));
    }

    @Test
    void tallyCountsAttributes() {
        List<GeneratedQuestion> selected = service.select(request(4), pool());
        Map<String, Integer> counts = service.tally(selected, GeneratedQuestion::getCourseOutcome);
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(4, total);
    }
}
