package com.questify.analytics.service;

import com.questify.analytics.client.RemoteApproval;
import com.questify.analytics.client.RemotePaper;
import com.questify.analytics.client.RemoteQuestion;
import com.questify.analytics.dto.ApprovalStats;
import com.questify.analytics.dto.CoverageSlice;
import com.questify.analytics.dto.MonthlyCount;
import com.questify.analytics.dto.PaperStats;
import com.questify.analytics.dto.QuestionStats;
import java.time.Duration;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Pure aggregation logic — no I/O, fully unit tested. */
@Component
public class StatsCalculator {

    public QuestionStats questionStats(List<RemoteQuestion> questions) {
        if (questions.isEmpty()) {
            return QuestionStats.empty();
        }
        return new QuestionStats(questions.size(),
                coverage(questions, question -> normalize(question.courseOutcome())),
                coverage(questions, question -> normalize(question.bloomLevel())));
    }

    public PaperStats paperStats(List<RemotePaper> papers, List<RemotePaper> generated) {
        Map<String, Long> perMonth = new TreeMap<>();
        for (RemotePaper paper : papers) {
            if (paper.createdAt() == null) {
                continue;
            }
            String month = YearMonth.from(paper.createdAt().atOffset(ZoneOffset.UTC)).toString();
            perMonth.merge(month, 1L, Long::sum);
        }
        List<MonthlyCount> monthly = perMonth.entrySet().stream()
                .map(entry -> new MonthlyCount(entry.getKey(), entry.getValue()))
                .toList();
        return new PaperStats(papers.size(), generated.size(), monthly);
    }

    public ApprovalStats approvalStats(List<RemoteApproval> approvals) {
        if (approvals.isEmpty()) {
            return ApprovalStats.empty();
        }
        long pending = approvals.stream().filter(a -> "PENDING".equalsIgnoreCase(a.status())).count();
        long approved = approvals.stream().filter(a -> "APPROVED".equalsIgnoreCase(a.status())).count();
        long rejected = approvals.stream().filter(a -> "REJECTED".equalsIgnoreCase(a.status())).count();

        List<Long> turnaroundMinutes = approvals.stream()
                .filter(a -> !"PENDING".equalsIgnoreCase(a.status()))
                .filter(a -> a.createdAt() != null && a.updatedAt() != null)
                .map(a -> Duration.between(a.createdAt(), a.updatedAt()).toMinutes())
                .filter(minutes -> minutes >= 0)
                .toList();
        Double averageHours = turnaroundMinutes.isEmpty() ? null
                : round(turnaroundMinutes.stream().mapToLong(Long::longValue).average().orElse(0) / 60.0);
        return new ApprovalStats(pending, approved, rejected, averageHours);
    }

    private List<CoverageSlice> coverage(List<RemoteQuestion> questions,
                                         Function<RemoteQuestion, String> classifier) {
        Map<String, Long> counts = questions.stream()
                .collect(Collectors.groupingBy(classifier, LinkedHashMap::new, Collectors.counting()));
        long total = questions.size();
        return counts.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> new CoverageSlice(entry.getKey(), entry.getValue(),
                        round(entry.getValue() * 100.0 / total)))
                .toList();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "UNSPECIFIED" : value.trim().toUpperCase();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
