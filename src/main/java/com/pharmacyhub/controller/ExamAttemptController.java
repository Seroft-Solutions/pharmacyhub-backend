package com.pharmacyhub.controller;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.dto.ExamResultDTO;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.response.ExamAttemptResponseDTO;
import com.pharmacyhub.dto.request.AnswerSubmissionDTO;
import com.pharmacyhub.dto.response.FlaggedQuestionResponseDTO;
import com.pharmacyhub.payment.manual.service.PaymentManualService;
import com.pharmacyhub.payment.service.PaymentService;
import com.pharmacyhub.service.ExamAttemptService;
import com.pharmacyhub.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
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
    private final ExamService examService;
    private final PaymentService paymentService;
    private final PaymentManualService paymentManualService;

    public ExamAttemptController(
            ExamAttemptService examAttemptService,
            ExamService examService,
            PaymentService paymentService,
            PaymentManualService paymentManualService) {
        this.examAttemptService = examAttemptService;
        this.examService = examService;
        this.paymentService = paymentService;
        this.paymentManualService = paymentManualService;
    }

    @PostMapping("/{examId}/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Start a new exam attempt")
    public ResponseEntity<ApiResponse<ExamAttemptResponseDTO>> startExam(
            @PathVariable Long examId,
            Authentication auth) {
        
        String userId = auth.getName();
        logger.info("User {} attempting to start exam {}", userId, examId);
        
        // Check if the exam is premium and if the user has purchased it
        try {
            Exam exam = examService.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with ID: " + examId));
            
            if (exam.isPremium()) {
                // COMPREHENSIVE APPROACH: CHECK ALL PAYMENT METHODS
                
                // 1. Check online payments: Has user purchased ANY premium exam? (pay once, access all)
                boolean hasPurchasedAnyExam = paymentService.hasUserPurchasedAnyExam(userId);
                
                // 2. Check manual payments: Has user had ANY manual payment request approved?
                boolean hasApprovedAnyManualPayment = paymentManualService.hasUserApprovedRequest(userId, null);
                
                // 3. If no universal access, check specific exam purchases
                boolean hasPurchasedThisExam = false;
                boolean hasApprovedThisExamManualPayment = false;
                
                if (!hasPurchasedAnyExam && !hasApprovedAnyManualPayment) {
                    // 3a. Check online payment for this specific exam
                    hasPurchasedThisExam = paymentService.hasUserPurchasedExam(examId, userId);
                    
                    // 3b. Check manual payment for this specific exam
                    hasApprovedThisExamManualPayment = paymentManualService.hasUserApprovedRequest(userId, examId);
                }
                
                // Grant access if ANY of these conditions are true
                boolean hasAccess = hasPurchasedAnyExam || 
                                 hasApprovedAnyManualPayment || 
                                 hasPurchasedThisExam || 
                                 hasApprovedThisExamManualPayment;
                
                // Detailed logging for troubleshooting
                logger.info("Premium access check for user {}, exam {}:\n" +
                           "  - Has purchased ANY exam (online): {}\n" +
                           "  - Has approved ANY manual payment: {}\n" +
                           "  - Has purchased THIS exam (online): {}\n" +
                           "  - Has approved THIS exam (manual): {}\n" +
                           "  - FINAL ACCESS DECISION: {}",
                    userId, examId, 
                    hasPurchasedAnyExam,
                    hasApprovedAnyManualPayment,
                    hasPurchasedThisExam,
                    hasApprovedThisExamManualPayment,
                    hasAccess);
                
                if (!hasAccess) {
                }
                
                logger.info("User {} GRANTED premium access to start exam {}.", userId, examId);
            }
            
            ExamAttemptResponseDTO attempt = examAttemptService.startExam(examId, userId);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.<ExamAttemptResponseDTO>success(attempt, 201));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Error starting exam: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
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
            // Use the atomic method to handle both answers and submission in a single transaction
            ExamResultDTO result = examAttemptService.submitExamWithAnswers(attemptId, finalAnswers);
            
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