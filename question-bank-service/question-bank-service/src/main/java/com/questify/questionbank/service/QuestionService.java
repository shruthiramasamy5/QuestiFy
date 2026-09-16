package com.questify.questionbank.service;

import com.questify.questionbank.dto.PageResponse;
import com.questify.questionbank.dto.QuestionRequest;
import com.questify.questionbank.dto.QuestionResponse;
import com.questify.questionbank.dto.QuestionSearchCriteria;
import com.questify.questionbank.entity.CourseOutcome;
import com.questify.questionbank.entity.Question;
import com.questify.questionbank.entity.QuestionCourseOutcome;
import com.questify.questionbank.exception.ResourceNotFoundException;
import com.questify.questionbank.repository.CourseOutcomeRepository;
import com.questify.questionbank.repository.QuestionRepository;
import com.questify.questionbank.security.CurrentUser;
import com.questify.questionbank.security.Roles;
import com.questify.questionbank.specification.QuestionSpecifications;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final CourseOutcomeRepository courseOutcomeRepository;
    private final QuestionMapper mapper;
    private final CurrentUser currentUser;

    public QuestionService(
            QuestionRepository questionRepository,
            CourseOutcomeRepository courseOutcomeRepository,
            QuestionMapper mapper,
            CurrentUser currentUser) {

        this.questionRepository = questionRepository;
        this.courseOutcomeRepository = courseOutcomeRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    public PageResponse<QuestionResponse> search(
            QuestionSearchCriteria criteria,
            Pageable pageable) {

        Page<Question> page =
                questionRepository.findAll(
                        QuestionSpecifications.withCriteria(criteria),
                        pageable
                );

        return PageResponse.from(
                page.map(mapper::toResponse)
        );
    }

    public QuestionResponse get(Long id) {
        return mapper.toResponse(findQuestion(id));
    }

    @Transactional
    public QuestionResponse create(QuestionRequest request) {

        Question question = new Question();

        apply(question, request);

        question.setCreatedBy(
                currentUser.username()
        );

        replaceOutcomes(
                question,
                request.courseOutcomeIds()
        );

        return mapper.toResponse(
                questionRepository.save(question)
        );
    }

    @Transactional
    public QuestionResponse update(
            Long id,
            QuestionRequest request) {

        Question question = findQuestion(id);

        assertCanModify(question);

        apply(question, request);

        replaceOutcomes(
                question,
                request.courseOutcomeIds()
        );

        return mapper.toResponse(
                questionRepository.save(question)
        );
    }

    @Transactional
    public void delete(Long id) {

        Question question = findQuestion(id);

        assertCanModify(question);

        questionRepository.delete(question);
    }

    Question findQuestion(Long id) {

        return questionRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Question " + id + " was not found"
                        )
                );
    }

    private void apply(
            Question question,
            QuestionRequest request) {

        question.setInstitutionId(
                request.institutionId()
        );

        question.setSubjectId(
                request.subjectId()
        );

        question.setQuestionText(
                request.questionText().trim()
        );

        question.setSubject(
                request.subject().trim()
        );

        question.setUnit(
                request.unit().trim()
        );

        question.setDifficulty(
                request.difficulty()
        );

        question.setQuestionType(
                request.questionType()
        );

        question.setMarks(
                request.marks()
        );

        question.setBloomLevel(
                request.bloomLevel()
        );
    }

    private void replaceOutcomes(
            Question question,
            Set<Long> courseOutcomeIds) {

        question.getCourseOutcomes().clear();

        if (courseOutcomeIds == null) {
            return;
        }

        for (Long coId : courseOutcomeIds) {

            CourseOutcome outcome =
                    courseOutcomeRepository
                            .findById(coId)
                            .orElseThrow(
                                    () -> new ResourceNotFoundException(
                                            "Course outcome "
                                                    + coId
                                                    + " was not found"
                                    )
                            );

            QuestionCourseOutcome mapping =
                    new QuestionCourseOutcome();

            mapping.setQuestion(question);
            mapping.setCourseOutcome(outcome);

            question.getCourseOutcomes().add(mapping);
        }
    }

    @Transactional
    public com.questify.questionbank.dto.BulkUploadResponse bulkUpload(java.util.List<QuestionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new com.questify.questionbank.dto.BulkUploadResponse(0, 0, 0, java.util.List.of("No questions provided"));
        }

        int imported = 0;
        int skipped = 0;
        java.util.List<String> errors = new java.util.ArrayList<>();

        Long fallbackInstId = currentUser.institutionId() != null ? currentUser.institutionId() : 1L;

        for (int i = 0; i < requests.size(); i++) {
            QuestionRequest req = requests.get(i);
            try {
                if (req.questionText() == null || req.questionText().isBlank()) {
                    skipped++;
                    errors.add("Row " + (i + 1) + ": Question text is required");
                    continue;
                }

                Question question = new Question();
                question.setInstitutionId(req.institutionId() != null ? req.institutionId() : fallbackInstId);
                question.setSubjectId(req.subjectId() != null ? req.subjectId() : 1L);
                question.setSubject(req.subject() != null && !req.subject().isBlank() ? req.subject() : "CS301");
                question.setUnit(req.unit() != null && !req.unit().isBlank() ? req.unit() : "Unit 1");
                question.setQuestionText(req.questionText());
                question.setQuestionType(req.questionType() != null ? req.questionType() : com.questify.questionbank.entity.QuestionType.SHORT_ANSWER);
                question.setDifficulty(req.difficulty() != null ? req.difficulty() : com.questify.questionbank.entity.Difficulty.MEDIUM);
                question.setMarks(req.marks() != null ? req.marks() : 2);
                question.setBloomLevel(req.bloomLevel() != null ? req.bloomLevel() : com.questify.questionbank.entity.BloomLevel.K2);
                question.setCreatedBy(currentUser.username() != null ? currentUser.username() : "faculty@questify.dev");

                if (req.courseOutcomeIds() != null && !req.courseOutcomeIds().isEmpty()) {
                    replaceOutcomes(question, req.courseOutcomeIds());
                }

                questionRepository.save(question);
                imported++;
            } catch (Exception ex) {
                skipped++;
                errors.add("Row " + (i + 1) + ": " + ex.getMessage());
            }
        }

        return new com.questify.questionbank.dto.BulkUploadResponse(requests.size(), imported, skipped, errors);
    }

    @Transactional
    public com.questify.questionbank.dto.SyllabusUploadResponse syllabusUpload(com.questify.questionbank.dto.SyllabusUploadRequest request) {
        String subject = request.subject() != null && !request.subject().isBlank() ? request.subject() : "CS301";
        java.util.List<String> units = (request.extractedUnits() != null && !request.extractedUnits().isEmpty())
                ? request.extractedUnits()
                : java.util.List.of("Unit 1: Fundamentals", "Unit 2: Core Models", "Unit 3: Normalization & Design", "Unit 4: Transactions", "Unit 5: Storage & Indexing");

        java.util.List<String> cos = java.util.List.of("CO1 (K2)", "CO2 (K3)", "CO3 (K6)", "CO4 (K4)", "CO5 (K5)");

        return new com.questify.questionbank.dto.SyllabusUploadResponse(
                subject,
                units.size(),
                units.size() * 3,
                units,
                cos
        );
    }

    private void assertCanModify(
            Question question) {

        if (currentUser.hasRole(
                Roles.COURSE_COORDINATOR)
                || currentUser.hasRole(Roles.HOD)) {

            return;
        }

        if (!question.getCreatedBy()
                .equals(currentUser.username())) {

            throw new AccessDeniedException(
                    "You can only modify questions you created"
            );
        }
    }
}