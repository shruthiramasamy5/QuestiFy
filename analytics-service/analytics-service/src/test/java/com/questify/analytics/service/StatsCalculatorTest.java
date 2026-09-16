package com.questify.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.questify.analytics.client.RemoteApproval;
import com.questify.analytics.client.RemotePaper;
import com.questify.analytics.client.RemoteQuestion;
import com.questify.analytics.dto.ApprovalStats;
import com.questify.analytics.dto.PaperStats;
import com.questify.analytics.dto.QuestionStats;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class StatsCalculatorTest {

    private final StatsCalculator calculator = new StatsCalculator();

    @Test
    void computesCoAndBloomCoverage() {
        QuestionStats stats = calculator.questionStats(List.of(
                new RemoteQuestion(1L, "CO1", "REMEMBER", "u1"),
                new RemoteQuestion(2L, "CO1", "APPLY", "u1"),
                new RemoteQuestion(3L, "CO2", "APPLY", "u2"),
                new RemoteQuestion(4L, null, null, "u2")));

        assertThat(stats.totalQuestions()).isEqualTo(4);
        assertThat(stats.coCoverage()).extracting("label").containsExactly("CO1", "CO2", "UNSPECIFIED");
        assertThat(stats.coCoverage().get(0).percentage()).isEqualTo(50.0);
        assertThat(stats.bloomCoverage()).extracting("label")
                .containsExactly("APPLY", "REMEMBER", "UNSPECIFIED");
    }

    @Test
    void emptyQuestionListYieldsEmptyStats() {
        assertThat(calculator.questionStats(List.of())).isEqualTo(QuestionStats.empty());
    }

    @Test
    void groupsPapersPerMonth() {
        PaperStats stats = calculator.paperStats(List.of(
                new RemotePaper(1L, "A", "APPROVED", "u1", Instant.parse("2026-01-05T10:00:00Z")),
                new RemotePaper(2L, "B", "APPROVED", "u1", Instant.parse("2026-01-25T10:00:00Z")),
                new RemotePaper(3L, "C", "DRAFT", "u2", Instant.parse("2026-02-01T10:00:00Z")),
                new RemotePaper(4L, "D", "DRAFT", "u2", null)),
                List.of(new RemotePaper(5L, "E", "GENERATED", "u1", Instant.now())));

        assertThat(stats.totalPapers()).isEqualTo(4);
        assertThat(stats.generatedPapers()).isEqualTo(1);
        assertThat(stats.papersPerMonth())
                .containsExactly(new com.questify.analytics.dto.MonthlyCount("2026-01", 2),
                        new com.questify.analytics.dto.MonthlyCount("2026-02", 1));
    }

    @Test
    void computesApprovalTurnaround() {
        ApprovalStats stats = calculator.approvalStats(List.of(
                new RemoteApproval(1L, 10L, "APPROVED", "COMPLETED", "u1",
                        Instant.parse("2026-03-01T00:00:00Z"), Instant.parse("2026-03-01T06:00:00Z")),
                new RemoteApproval(2L, 11L, "REJECTED", "HOD", "u1",
                        Instant.parse("2026-03-02T00:00:00Z"), Instant.parse("2026-03-02T02:00:00Z")),
                new RemoteApproval(3L, 12L, "PENDING", "HOD", "u2",
                        Instant.parse("2026-03-03T00:00:00Z"), null)));

        assertThat(stats.approved()).isEqualTo(1);
        assertThat(stats.rejected()).isEqualTo(1);
        assertThat(stats.pending()).isEqualTo(1);
        assertThat(stats.averageTurnaroundHours()).isEqualTo(4.0);
    }

    @Test
    void turnaroundIsNullWhenNothingWasDecided() {
        ApprovalStats stats = calculator.approvalStats(List.of(
                new RemoteApproval(1L, 10L, "PENDING", "HOD", "u1", Instant.now(), null)));
        assertThat(stats.averageTurnaroundHours()).isNull();
    }
}
