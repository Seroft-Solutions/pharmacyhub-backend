package com.pharmacyhub.controller;

import com.pharmacyhub.controller.base.BaseController;
import com.pharmacyhub.domain.entity.Question;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.response.QuestionResponseDTO;
import com.pharmacyhub.service.ExamService;
import com.pharmacyhub.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/questions")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Questions", description = "API endpoints for question management")
public class QuestionController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    private final QuestionService questionService;
    private final ExamService examService;

    public QuestionController(QuestionService questionService, ExamService examService) {
        this.questionService = questionService;
        this.examService = examService;
    }

    @GetMapping("/exam/{examId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR') or @examAccessEvaluator.canAccessExam(authentication, #examId)")
    @Operation(summary = "Get questions for an exam")
    public ResponseEntity<ApiResponse<List<QuestionResponseDTO>>> getQuestionsByExamId(@PathVariable Long examId) {
        try {
            logger.info("Fetching questions for exam ID: {}", examId);
            List<Question> questions = questionService.findByExamId(examId);
            List<QuestionResponseDTO> responseDTO = questions.stream()
                .map(this::mapToQuestionResponseDTO)
                .collect(Collectors.toList());
            logger.info("Successfully fetched {} questions for exam ID: {}", questions.size(), examId);
            return ResponseEntity.ok(ApiResponse.success(responseDTO));
        } catch (EntityNotFoundException e) {
            logger.error("Error fetching questions for exam ID {}: {}", examId, e.getMessage());
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error fetching questions for exam ID {}: {}", examId, e.getMessage(), e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch questions", e);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR') or @questionAccessEvaluator.canAccessQuestion(authentication, #id)")
    @Operation(summary = "Get question by ID")
    public ResponseEntity<ApiResponse<QuestionResponseDTO>> getQuestionById(@PathVariable Long id) {
        try {
            logger.info("Fetching question with ID: {}", id);
            Question question = questionService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Question not found with ID: " + id));
            QuestionResponseDTO responseDTO = mapToQuestionResponseDTO(question);
            logger.info("Successfully fetched question with ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(responseDTO));
        } catch (EntityNotFoundException e) {
            logger.error("Error fetching question with ID {}: {}", id, e.getMessage());
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error fetching question with ID {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch question", e);
        }
    }

    @GetMapping("/topic/{topic}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'USER')")
    @Operation(summary = "Get questions by topic")
    public ResponseEntity<ApiResponse<List<QuestionResponseDTO>>> getQuestionsByTopic(@PathVariable String topic) {
        try {
            logger.info("Fetching questions for topic: {}", topic);
            List<Question> questions = questionService.findByTopic(topic);
            List<QuestionResponseDTO> responseDTO = questions.stream()
                .map(this::mapToQuestionResponseDTO)
                .collect(Collectors.toList());
            logger.info("Successfully fetched {} questions for topic: {}", questions.size(), topic);
            return ResponseEntity.ok(ApiResponse.success(responseDTO));
        } catch (Exception e) {
            logger.error("Error fetching questions for topic {}: {}", topic, e.getMessage(), e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch questions by topic", e);
        }
    }

    @GetMapping("/difficulty/{level}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'USER')")
    @Operation(summary = "Get questions by difficulty level")
    public ResponseEntity<ApiResponse<List<QuestionResponseDTO>>> getQuestionsByDifficulty(@PathVariable String level) {
        try {
            logger.info("Fetching questions with difficulty level: {}", level);
            List<Question> questions = questionService.findByDifficulty(level);
            List<QuestionResponseDTO> responseDTO = questions.stream()
                .map(this::mapToQuestionResponseDTO)
                .collect(Collectors.toList());
            logger.info("Successfully fetched {} questions with difficulty level: {}", questions.size(), level);
            return ResponseEntity.ok(ApiResponse.success(responseDTO));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid difficulty level {}: {}", level, e.getMessage());
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error fetching questions with difficulty level {}: {}", level, e.getMessage(), e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch questions by difficulty", e);
        }
    }

    @GetMapping("/random")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'USER')")
    @Operation(summary = "Get random questions")
    public ResponseEntity<ApiResponse<List<QuestionResponseDTO>>> getRandomQuestions(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String difficulty) {
        try {
            logger.info("Fetching {} random questions with topic: {}, difficulty: {}", count, topic, difficulty);
            List<Question> questions = questionService.findRandom(count, topic, difficulty);
            List<QuestionResponseDTO> responseDTO = questions.stream()
                .map(this::mapToQuestionResponseDTO)
                .collect(Collectors.toList());
            logger.info("Successfully fetched {} random questions", questions.size());
            return ResponseEntity.ok(ApiResponse.success(responseDTO));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for random questions: {}", e.getMessage());
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error fetching random questions: {}", e.getMessage(), e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch random questions", e);
        }
    }
    
    private QuestionResponseDTO mapToQuestionResponseDTO(Question question) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setId(question.getId());
        dto.setQuestionNumber(question.getQuestionNumber());
        dto.setText(question.getQuestionText());
        // Don't include the correct answer in the response for security
        dto.setExplanation(question.getExplanation());
        dto.setPoints(question.getMarks());
        
        // Map options without revealing which is correct
        if (question.getOptions() != null) {
            List<QuestionResponseDTO.OptionDTO> optionDTOs = question.getOptions().stream()
                    .map(option -> {
                        QuestionResponseDTO.OptionDTO optionDTO = new QuestionResponseDTO.OptionDTO();
                        optionDTO.setId(option.getId());
                        optionDTO.setLabel(option.getLabel());
                        optionDTO.setText(option.getText());
                        // Don't include isCorrect flag for security
                        return optionDTO;
                    })
                    .collect(Collectors.toList());
            dto.setOptions(optionDTOs);
        }
        
        return dto;
    }
}