package com.questify.questionbank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.questify.questionbank.dto.CourseOutcomeRequest;
import com.questify.questionbank.dto.QuestionRequest;
import com.questify.questionbank.entity.BloomLevel;
import com.questify.questionbank.entity.Difficulty;
import com.questify.questionbank.entity.QuestionType;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest
@ActiveProfiles("test")
class QuestionBankApiTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/questions")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "faculty@questify.edu", roles = {"FACULTY"})
    void createsAndFiltersQuestions() throws Exception {
        String coJson = objectMapper.writeValueAsString(
                new CourseOutcomeRequest("CO1", "Apply data structures", "Data Structures", BloomLevel.K3));

        // Faculty cannot create course outcomes.
        mockMvc.perform(post("/api/co-mapping/course-outcomes")
                        .contentType(MediaType.APPLICATION_JSON).content(coJson))
                .andExpect(status().isForbidden());

        String createdCo = mockMvc.perform(post("/api/co-mapping/course-outcomes")
                        .with(user("coordinator@questify.edu").roles("COURSE_COORDINATOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(coJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long coId = objectMapper.readTree(createdCo).get("id").asLong();

        QuestionRequest question = new QuestionRequest(1L, 1L, "Explain AVL rotations.", "Data Structures", "Unit 2",
                Difficulty.MEDIUM, QuestionType.LONG_ANSWER, 10, BloomLevel.K3, Set.of(coId));

        mockMvc.perform(post("/api/questions").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(question)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseOutcomes[0].code").value("CO1"))
                .andExpect(jsonPath("$.createdBy").value("faculty@questify.edu"));

        mockMvc.perform(get("/api/questions")
                        .param("subject", "data structures")
                        .param("bloomLevel", "K3")
                        .param("difficulty", "MEDIUM")
                        .param("courseOutcomeCode", "CO1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].marks").value(10));

        mockMvc.perform(get("/api/questions").param("bloomLevel", "K6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        assertThat(coId).isPositive();
    }

    @Test
    @WithMockUser(username = "faculty@questify.edu", roles = {"FACULTY"})
    void rejectsInvalidQuestion() throws Exception {
        QuestionRequest invalid = new QuestionRequest(null, null, " ", "", "", null, null, 0, null, null);
        mockMvc.perform(post("/api/questions").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
