package com.questify.questionbank.service;

import com.questify.questionbank.dto.CourseOutcomeRequest;
import com.questify.questionbank.dto.CourseOutcomeResponse;
import com.questify.questionbank.dto.PageResponse;
import com.questify.questionbank.dto.QuestionCoMappingRequest;
import com.questify.questionbank.dto.QuestionCoMappingResponse;
import com.questify.questionbank.entity.CourseOutcome;
import com.questify.questionbank.entity.Question;
import com.questify.questionbank.entity.QuestionCourseOutcome;
import com.questify.questionbank.exception.DuplicateResourceException;
import com.questify.questionbank.exception.ResourceNotFoundException;
import com.questify.questionbank.repository.CourseOutcomeRepository;
import com.questify.questionbank.repository.QuestionCourseOutcomeRepository;
import com.questify.questionbank.repository.QuestionRepository;
import com.questify.questionbank.security.CurrentUser;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class CoMappingService {

    private final CourseOutcomeRepository courseOutcomeRepository;
    private final QuestionRepository questionRepository;
    private final QuestionCourseOutcomeRepository mappingRepository;
    private final QuestionMapper mapper;
    private final CurrentUser currentUser;

    public CoMappingService(CourseOutcomeRepository courseOutcomeRepository,
                            QuestionRepository questionRepository,
                            QuestionCourseOutcomeRepository mappingRepository,
                            QuestionMapper mapper,
                            CurrentUser currentUser) {
        this.courseOutcomeRepository = courseOutcomeRepository;
        this.questionRepository = questionRepository;
        this.mappingRepository = mappingRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    public PageResponse<CourseOutcomeResponse> listOutcomes(String subject, Pageable pageable) {
        Page<CourseOutcome> page = StringUtils.hasText(subject)
                ? courseOutcomeRepository.findBySubjectIgnoreCase(subject, pageable)
                : courseOutcomeRepository.findAll(pageable);
        return PageResponse.from(page.map(mapper::toResponse));
    }

    @Transactional
    public CourseOutcomeResponse createOutcome(CourseOutcomeRequest request) {
        if (courseOutcomeRepository.existsBySubjectIgnoreCaseAndCodeIgnoreCase(request.subject(), request.code())) {
            throw new DuplicateResourceException(
                    request.code() + " already exists for subject " + request.subject());
        }
        CourseOutcome outcome = new CourseOutcome();
        apply(outcome, request);
        outcome.setCreatedBy(currentUser.username());
        return mapper.toResponse(courseOutcomeRepository.save(outcome));
    }

    @Transactional
    public CourseOutcomeResponse updateOutcome(Long id, CourseOutcomeRequest request) {
        CourseOutcome outcome = findOutcome(id);
        courseOutcomeRepository.findBySubjectIgnoreCaseAndCodeIgnoreCase(request.subject(), request.code())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            request.code() + " already exists for subject " + request.subject());
                });
        apply(outcome, request);
        return mapper.toResponse(courseOutcomeRepository.save(outcome));
    }

    @Transactional
    public void deleteOutcome(Long id) {
        CourseOutcome outcome = findOutcome(id);
        if (mappingRepository.existsByCourseOutcomeId(id)) {
            throw new DuplicateResourceException("Course outcome is mapped to questions and cannot be deleted");
        }
        courseOutcomeRepository.delete(outcome);
    }

    public QuestionCoMappingResponse getMappings(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question " + questionId + " was not found"));
        return toMappingResponse(question);
    }

    @Transactional
    public QuestionCoMappingResponse map(QuestionCoMappingRequest request) {
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question " + request.questionId() + " was not found"));
        for (Long coId : request.courseOutcomeIds()) {
            CourseOutcome outcome = findOutcome(coId);
            boolean alreadyMapped = question.getCourseOutcomes().stream()
                    .anyMatch(mapping -> mapping.getCourseOutcome().getId().equals(coId));
            if (!alreadyMapped) {
                QuestionCourseOutcome mapping = new QuestionCourseOutcome();
                mapping.setQuestion(question);
                mapping.setCourseOutcome(outcome);
                question.getCourseOutcomes().add(mapping);
            }
        }
        return toMappingResponse(questionRepository.save(question));
    }

    @Transactional
    public void unmap(Long questionId, Long courseOutcomeId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question " + questionId + " was not found"));
        boolean removed = question.getCourseOutcomes()
                .removeIf(mapping -> mapping.getCourseOutcome().getId().equals(courseOutcomeId));
        if (!removed) {
            throw new ResourceNotFoundException("Question " + questionId + " is not mapped to CO " + courseOutcomeId);
        }
        questionRepository.save(question);
    }

    private QuestionCoMappingResponse toMappingResponse(Question question) {
        List<CourseOutcomeResponse> outcomes = question.getCourseOutcomes().stream()
                .map(QuestionCourseOutcome::getCourseOutcome)
                .sorted(Comparator.comparing(CourseOutcome::getCode))
                .map(mapper::toResponse)
                .toList();
        return new QuestionCoMappingResponse(question.getId(), question.getQuestionText(), outcomes);
    }

    private CourseOutcome findOutcome(Long id) {
        return courseOutcomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course outcome " + id + " was not found"));
    }

    private void apply(CourseOutcome outcome, CourseOutcomeRequest request) {
        outcome.setCode(request.code().trim().toUpperCase());
        outcome.setDescription(request.description().trim());
        outcome.setSubject(request.subject().trim());
        outcome.setBloomLevel(request.bloomLevel());
    }
}
