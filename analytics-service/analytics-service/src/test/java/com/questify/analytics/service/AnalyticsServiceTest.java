package com.questify.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.questify.analytics.client.RemoteApproval;
import com.questify.analytics.client.RemotePaper;
import com.questify.analytics.client.RemoteQuestion;
import com.questify.analytics.client.UpstreamClient;
import com.questify.analytics.dto.AnalyticsOverview;
import com.questify.analytics.security.AuthenticatedUser;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnalyticsServiceTest {

    @Mock
    private UpstreamClient upstreamClient;

    private AnalyticsService service;

    private final AuthenticatedUser faculty =
            new AuthenticatedUser("u-fac", "fac@questify.test", "inst-1", Set.of("FACULTY"));
    private final AuthenticatedUser hod =
            new AuthenticatedUser("u-hod", "hod@questify.test", "inst-1", Set.of("HOD"));
    private final AuthenticatedUser reviewer =
            new AuthenticatedUser("u-rev", "rev@questify.test", "inst-1", Set.of("REVIEWER"));

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(upstreamClient, new StatsCalculator());
        when(upstreamClient.questions(anyString())).thenReturn(List.of(
                new RemoteQuestion(1L, "CO1", "APPLY", "u-fac"),
                new RemoteQuestion(2L, "CO2", "ANALYZE", "u-other")));
        when(upstreamClient.papers(anyString())).thenReturn(List.of(
                new RemotePaper(1L, "Own", "APPROVED", "u-fac", Instant.parse("2026-04-01T00:00:00Z")),
                new RemotePaper(2L, "Other", "APPROVED", "u-other", Instant.parse("2026-04-02T00:00:00Z"))));
        when(upstreamClient.generatedPapers(anyString())).thenReturn(List.of(
                new RemotePaper(3L, "Gen", "GENERATED", "u-fac", Instant.parse("2026-04-03T00:00:00Z"))));
        when(upstreamClient.approvals(anyString())).thenReturn(List.of(
                new RemoteApproval(1L, 1L, "APPROVED", "COMPLETED", "u-fac",
                        Instant.parse("2026-04-01T00:00:00Z"), Instant.parse("2026-04-01T03:00:00Z")),
                new RemoteApproval(2L, 2L, "PENDING", "HOD", "u-other",
                        Instant.parse("2026-04-02T00:00:00Z"), null)));
    }

    @Test
    void facultyScopeOnlyIncludesOwnData() {
        AnalyticsOverview overview = service.overview(faculty, "jwt");

        assertThat(overview.scope()).isEqualTo("FACULTY");
        assertThat(overview.questions().totalQuestions()).isEqualTo(1);
        assertThat(overview.papers().totalPapers()).isEqualTo(1);
        assertThat(overview.approvals().approved()).isEqualTo(1);
        assertThat(overview.approvals().pending()).isZero();
    }

    @Test
    void hodScopeIncludesEverything() {
        AnalyticsOverview overview = service.overview(hod, "jwt");

        assertThat(overview.scope()).isEqualTo("DEPARTMENT");
        assertThat(overview.questions().totalQuestions()).isEqualTo(2);
        assertThat(overview.papers().totalPapers()).isEqualTo(2);
        assertThat(overview.approvals().pending()).isEqualTo(1);
        assertThat(overview.degradedSources()).isEmpty();
    }

    @Test
    void otherRolesAreRejected() {
        assertThatThrownBy(() -> service.overview(reviewer, "jwt"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void turnaroundEndpointIsHodOnly() {
        assertThatThrownBy(() -> service.approvals(faculty, "jwt"))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(service.approvals(hod, "jwt").averageTurnaroundHours()).isEqualTo(3.0);
    }
}
