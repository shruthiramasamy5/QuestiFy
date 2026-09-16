package com.questify.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.questify.audit.domain.AuditEvent;
import com.questify.audit.dto.AuditEventQuery;
import com.questify.audit.dto.AuditEventResponse;
import com.questify.audit.dto.CreateAuditEventRequest;
import com.questify.audit.repository.AuditEventRepository;
import com.questify.audit.security.AuthenticatedUser;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    @Mock
    private AuditEventRepository repository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Captor
    private ArgumentCaptor<Query> queryCaptor;

    private AuditEventService service;

    private final AuthenticatedUser superAdmin =
            new AuthenticatedUser("u-sa", "sa@questify.test", null, Set.of("SUPER_ADMIN"));
    private final AuthenticatedUser institutionAdmin =
            new AuthenticatedUser("u-ia", "ia@questify.test", "inst-1", Set.of("INSTITUTION_ADMIN"));

    @BeforeEach
    void setUp() {
        service = new AuditEventService(repository, mongoTemplate);
    }

    @Test
    void appendFillsActorFromJwtAndKeepsFlexibleMetadata() {
        when(repository.save(any(AuditEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        AuditEventResponse response = service.append(institutionAdmin,
                new CreateAuditEventRequest(null, null, "paper.approved", "PAPER", "42", null,
                        Map.of("stage", "HOD", "nested", Map.of("comment", "ok"))));

        assertThat(response.actorUserId()).isEqualTo("u-ia");
        assertThat(response.actorRole()).isEqualTo("INSTITUTION_ADMIN");
        assertThat(response.action()).isEqualTo("PAPER.APPROVED");
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.metadata()).containsKey("nested");
    }

    @Test
    void appendKeepsExplicitActorAndTimestamp() {
        when(repository.save(any(AuditEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        Instant when = Instant.parse("2026-05-01T10:00:00Z");

        AuditEventResponse response = service.append(superAdmin,
                new CreateAuditEventRequest("u-faculty", "faculty", "LOGIN", "USER", "u-faculty", when, null));

        assertThat(response.actorUserId()).isEqualTo("u-faculty");
        assertThat(response.actorRole()).isEqualTo("FACULTY");
        assertThat(response.timestamp()).isEqualTo(when);
        assertThat(response.metadata()).isEmpty();
    }

    @Test
    void searchAppliesActorActionAndDateFilters() {
        when(mongoTemplate.find(queryCaptor.capture(), eq(AuditEvent.class))).thenReturn(List.of());

        service.search(superAdmin, new AuditEventQuery("u-faculty", "login",
                Instant.parse("2026-05-01T00:00:00Z"), Instant.parse("2026-05-31T00:00:00Z"), 0, 50));

        Document document = queryCaptor.getValue().getQueryObject();
        assertThat(document.get("actorUserId")).isEqualTo("u-faculty");
        assertThat(document.get("action")).isEqualTo("LOGIN");
        assertThat(document).containsKey("timestamp");
        assertThat(document).doesNotContainKey("institutionId");
    }

    @Test
    void institutionAdminIsScopedToOwnInstitution() {
        when(mongoTemplate.find(queryCaptor.capture(), eq(AuditEvent.class))).thenReturn(List.of());

        service.search(institutionAdmin, new AuditEventQuery(null, null, null, null, 0, 20));

        assertThat(queryCaptor.getValue().getQueryObject().get("institutionId")).isEqualTo("inst-1");
    }
}
