package com.pharmacyhub.controller;

import com.pharmacyhub.dto.ExamResultDTO;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.response.ExamAttemptResponseDTO;
import com.pharmacyhub.dto.request.AnswerSubmissionDTO;
import com.pharmacyhub.dto.response.FlaggedQuestionResponseDTO;
import com.pharmacyhub.service.ExamAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Exam Attempts", description = "API endpoints for exam attempts")
public class ExamAttemptController {

    private static final Logger logger = LoggerFactory.getLogger(ExamAttemptController.class);
    private final ExamAttemptService examAttemptService;

    public ExamAttemptController(ExamAttemptService examAttemptService) {
        this.examAttemptService = examAttemptService;
    }

    @PostMapping("/{examId}/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Start a new exam attempt")
    public ResponseEntity<ApiResponse<ExamAttemptResponseDTO>> startExam(
            @PathVariable Long examId,
            Authentication auth) {
        
        String userId = auth.getName();
        logger.info("User {} attempting to start exam {}", userId, examId);
        
        ExamAttemptResponseDTO attempt = examAttemptService.startExam(examId, userId);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<ExamAttemptResponseDTO>success(attempt, 201));
    }

    @GetMapping("/attempts/user")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's exam attempts")
    public ResponseEntity<ApiResponse<List<ExamAttemptResponseDTO>>> getCurrentUserAttempts(Authentication auth) {
        String userId = auth.getName();
        logger.info("Fetching exam attempts for user: {}", userId);
        
        List<ExamAttemptResponseDTO> attempts = examAttemptService.getAttemptsByUserId(userId);
        
        return ResponseEntity.ok(ApiResponse.<List<ExamAttemptResponseDTO>>success(attempts));
    }

    @GetMapping("/{examId}/attempts")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's attempts for a specific exam")
    public ResponseEntity<ApiResponse<List<ExamAttemptResponseDTO>>> getExamAttempts(
            @PathVariable Long examId,
            Authentication auth) {
        
        String userId = auth.getName();
        logger.info("Fetching attempts for exam {} by user {}", examId, userId);
        
        List<ExamAttemptResponseDTO> attempts = examAttemptService.getAttemptsByExamAndUserId(examId, userId);
        
        return ResponseEntity.ok(ApiResponse.<List<ExamAttemptResponseDTO>>success(attempts));
    }

    @GetMapping("/attempts/{attemptId}")
    @PreAuthorize("isAuthenticated() and @examAccessEvaluator.canAccessAttempt(authentication, #attemptId)")
    @Operation(summary = "Get a specific exam attempt")
    public ResponseEntity<ApiResponse<ExamAttemptResponseDTO>> getAttempt(@PathVariable Long attemptId) {
        logger.info("Fetching exam attempt with ID: {}", attemptId);
        
        ExamAttemptResponseDTO attempt = examAttemptService.getAttemptById(attemptId);
        
        return ResponseEntity.ok(ApiResponse.<ExamAttemptResponseDTO>success(attempt));
    }

    @PostMapping("/attempts/{attemptId}/answer/{questionId}")
    @PreAuthorize("isAuthenticated() and @examAccessEvaluator.canAccessAttempt(authentication, #attemptId)")
    @Operation(summary = "Submit an answer for a question")
    public ResponseEntity<ApiResponse<Void>> submitAnswer(
            @PathVariable Long attemptId,
            @PathVariable Long questionId,
            @RequestBody @Valid AnswerSubmissionDTO answerDTO) {
        
        logger.info("Submitting answer for question {} in attempt {}", questionId, attemptId);
        
        examAttemptService.saveAnswer(attemptId, questionId, answerDTO.getSelectedOptionId(), answerDTO.getTimeSpent());
        
        return ResponseEntity.ok(ApiResponse.<Void>success(null));
    }

    @PostMapping("/attempts/{attemptId}/submit")
    @PreAuthorize("isAuthenticated() and @examAccessEvaluator.canAccessAttempt(authentication, #attemptId)")
    @Operation(summary = "Submit an exam attempt")
    public ResponseEntity<ApiResponse<ExamResultDTO>> submitExam(
            @PathVariable Long attemptId,
            @RequestBody(required = false) List<AnswerSubmissionDTO> finalAnswers,
            Authentication auth) {
        
        String userId = auth.getName();
        logger.info("User {} submitting exam attempt {}", userId, attemptId);
        
        try {
            // Process any final answers if provided
            if (finalAnswers != null && !finalAnswers.isEmpty()) {
                for (AnswerSubmissionDTO answer : finalAnswers) {
                    examAttemptService.saveAnswer(
                        attemptId, 
                        answer.getQuestionId(), 
                        answer.getSelectedOptionId(), 
                        answer.getTimeSpent()
                    );
                }
            }
            
            // Submit the exam and get results
            ExamResultDTO result = examAttemptService.submitExamAttempt(attemptId);
            
            return ResponseEntity.ok(ApiResponse.<ExamResultDTO>success(result));
        } catch (Exception e) {
            logger.error("Error submitting exam: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/attempts/{attemptId}/result")
    @PreAuthorize("isAuthenticated() and @examAccessEvaluator.canAccessAttempt(authentication, #attemptId)")
    @Operation(summary = "Get the result for a completed exam")
    public ResponseEntity<ApiResponse<ExamResultDTO>> getExamResult(@PathVariable Long attemptId) {
        logger.info("Fetching result for exam attempt: {}", attemptId);
        
        try {
            ExamResultDTO result = examAttemptService.getExamResult(attemptId);
            return ResponseEntity.ok(ApiResponse.<ExamResultDTO>success(result));
        } catch (Exception e) {
            logger.error("Error fetching exam result: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping("/attempts/{attemptId}/flag/{questionId}")
    @PreAuthorize("isAuthenticated() and @examAccessEvaluator.canAccessAttempt(authentication, #attemptId)")
    @Operation(summary = "Flag a question for review")
    public ResponseEntity<ApiResponse<Void>> flagQuestion(
            @PathVariable Long attemptId,
            @PathVariable Long questionId) {
        
        logger.info("Flagging question {} in attempt {}", questionId, attemptId);
        
        examAttemptService.flagQuestion(attemptId, questionId);
        
        return ResponseEntity.ok(ApiResponse.<Void>success(null));
    }

    @DeleteMapping("/attempts/{attemptId}/flag/{questionId}")
    @PreAuthorize("isAuthenticated() and @examAccessEvaluator.canAccessAttempt(authentication, #attemptId)")
    @Operation(summary = "Unflag a previously flagged question")
    public ResponseEntity<ApiResponse<Void>> unflagQuestion(
            @PathVariable Long attemptId,
            @PathVariable Long questionId) {
        
        logger.info("Unflagging question {} in attempt {}", questionId, attemptId);
        
        examAttemptService.unflagQuestion(attemptId, questionId);
        
        return ResponseEntity.ok(ApiResponse.<Void>success(null));
    }

    @GetMapping("/attempts/{attemptId}/flags")
    @PreAuthorize("isAuthenticated() and @examAccessEvaluator.canAccessAttempt(authentication, #attemptId)")
    @Operation(summary = "Get all flagged questions for an attempt")
    public ResponseEntity<ApiResponse<List<FlaggedQuestionResponseDTO>>> getFlaggedQuestions(
            @PathVariable Long attemptId) {
        
        logger.info("Fetching flagged questions for attempt {}", attemptId);
        
        List<FlaggedQuestionResponseDTO> flaggedQuestions = examAttemptService.getFlaggedQuestions(attemptId);
        
        return ResponseEntity.ok(ApiResponse.<List<FlaggedQuestionResponseDTO>>success(flaggedQuestions));
    }
}