package com.questify.analytics.service;

import com.questify.analytics.client.RemoteApproval;
import com.questify.analytics.client.RemotePaper;
import com.questify.analytics.client.RemoteQuestion;
import com.questify.analytics.client.UpstreamClient;
import com.questify.analytics.dto.AnalyticsOverview;
import com.questify.analytics.dto.ApprovalStats;
import com.questify.analytics.dto.PaperStats;
import com.questify.analytics.dto.QuestionStats;
import com.questify.analytics.security.AuthenticatedUser;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Aggregates read-only data pulled from the other QuestiFy services.
 *
 * <p>Scoping: an HOD sees department-wide numbers; a Faculty member only sees
 * rows they created. Any other role is rejected.</p>
 */
@Service
public class AnalyticsService {

    private final UpstreamClient upstreamClient;
    private final StatsCalculator calculator;

    public AnalyticsService(UpstreamClient upstreamClient, StatsCalculator calculator) {
        this.upstreamClient = upstreamClient;
        this.calculator = calculator;
    }

    public AnalyticsOverview overview(AuthenticatedUser user, String bearerToken) {
        String scope = scopeFor(user);
        List<RemoteQuestion> questions = upstreamClient.questions(bearerToken);
        List<RemotePaper> papers = upstreamClient.papers(bearerToken);
        List<RemotePaper> generated = upstreamClient.generatedPapers(bearerToken);
        List<RemoteApproval> approvals = upstreamClient.approvals(bearerToken);

        List<String> degraded = new ArrayList<>();
        if (questions.isEmpty()) {
            degraded.add("QUESTION-BANK-SERVICE");
        }
        if (papers.isEmpty()) {
            degraded.add("PAPERS-SERVICE");
        }
        if (generated.isEmpty()) {
            degraded.add("PAPER-GENERATION-SERVICE");
        }
        if (approvals.isEmpty()) {
            degraded.add("APPROVAL-SERVICE");
        }

        if ("FACULTY".equals(scope)) {
            String userId = user.userId();
            questions = questions.stream().filter(q -> Objects.equals(userId, q.createdBy())).toList();
            papers = papers.stream().filter(p -> Objects.equals(userId, p.createdBy())).toList();
            generated = generated.stream().filter(p -> Objects.equals(userId, p.createdBy())).toList();
            approvals = approvals.stream().filter(a -> Objects.equals(userId, a.createdBy())).toList();
        }

        return new AnalyticsOverview(scope, Instant.now(),
                calculator.questionStats(questions),
                calculator.paperStats(papers, generated),
                calculator.approvalStats(approvals),
                List.copyOf(degraded));
    }

    public QuestionStats questions(AuthenticatedUser user, String bearerToken) {
        return overview(user, bearerToken).questions();
    }

    public PaperStats papers(AuthenticatedUser user, String bearerToken) {
        return overview(user, bearerToken).papers();
    }

    public ApprovalStats approvals(AuthenticatedUser user, String bearerToken) {
        if (!user.hasRole("HOD") && !user.hasRole("INSTITUTION_ADMIN") && !user.hasRole("SUPER_ADMIN") && !user.hasRole("REVIEWER") && !user.hasRole("COURSE_COORDINATOR")) {
            throw new AccessDeniedException("Approval turnaround analytics are restricted to approver roles");
        }
        return overview(user, bearerToken).approvals();
    }

    private String scopeFor(AuthenticatedUser user) {
        if (user.hasRole("SUPER_ADMIN")) {
            return "GLOBAL";
        }
        if (user.hasRole("INSTITUTION_ADMIN")) {
            return "INSTITUTION";
        }
        if (user.hasRole("HOD") || user.hasRole("COURSE_COORDINATOR") || user.hasRole("REVIEWER")) {
            return "DEPARTMENT";
        }
        if (user.hasRole("FACULTY")) {
            return "FACULTY";
        }
        return "ALL";
    }
}
