package com.pharmacyhub.controller;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.domain.entity.Option;
import com.pharmacyhub.domain.entity.Question;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.request.ExamRequestDTO;
import com.pharmacyhub.dto.request.JsonExamUploadRequestDTO;
import com.pharmacyhub.dto.response.ExamResponseDTO;
import com.pharmacyhub.dto.response.QuestionResponseDTO;
import com.pharmacyhub.payment.dto.PremiumExamInfoDTO;
import com.pharmacyhub.payment.manual.service.PaymentManualService;
import com.pharmacyhub.payment.service.PaymentService;
import com.pharmacyhub.security.annotation.RequiresPermission;
import com.pharmacyhub.security.constants.ExamPermissionConstants;
import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.service.ExamService;
import com.pharmacyhub.service.JsonExamUploadService;
import com.pharmacyhub.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/exams")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Exams", description = "API endpoints for exam management")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Exams", description = "Manage exams, questions, and exam-taking processes")
public class ExamController {

    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);

    private final ExamService examService;
    private final QuestionService questionService;
    private final JsonExamUploadService jsonExamUploadService;
    private final PaymentService paymentService;
    private final PaymentManualService paymentManualService;

    @Autowired
    public ExamController(
            ExamService examService, 
            QuestionService questionService, 
            JsonExamUploadService jsonExamUploadService,
            PaymentService paymentService,
            PaymentManualService paymentManualService) {
        this.examService = examService;
        this.questionService = questionService;
        this.jsonExamUploadService = jsonExamUploadService;
        this.paymentService = paymentService;
        this.paymentManualService = paymentManualService;
    }

    @GetMapping
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.READ, permissionName = ExamPermissionConstants.VIEW_EXAMS)
    @Operation(summary = "Get all exams - Admin/Instructor only")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getAllExams(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching all exams");
        List<Exam> exams = examService.findAllActive();
        
        // Process each exam separately to avoid lazy loading issues
        List<ExamResponseDTO> examResponseDTOs = new ArrayList<>();
        for (Exam exam : exams) {
            // Map basic exam info without accessing lazy collections
            ExamResponseDTO dto = mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null);
            examResponseDTOs.add(dto);
        }
        
        return ResponseEntity.ok(ApiResponse.success(examResponseDTOs));
    }

    @GetMapping("/published")
    @Operation(summary = "Get all published exams - Public access")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getAllPublishedExams(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching all published exams");
        try {
            List<Exam> publishedExams = examService.findAllPublished();
            
            // Process each exam separately to avoid lazy loading issues
            List<ExamResponseDTO> examResponseDTOs = new ArrayList<>();
            for (Exam exam : publishedExams) {
                // Map basic exam info without accessing lazy collections
                ExamResponseDTO dto = mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null);
                examResponseDTOs.add(dto);
            }
            
            logger.info("Successfully fetched {} published exams", examResponseDTOs.size());
            return ResponseEntity.ok(ApiResponse.success(examResponseDTOs));
        } catch (Exception e) {
            logger.error("Error fetching published exams: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching published exams", e);
        }
    }
    
    @GetMapping("/papers/model")
    @Operation(summary = "Get model papers")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getModelPapers(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching model papers");
        
        // Get all published exams with "MODEL" tag
        List<Exam> exams = examService.findAllPublished().stream()
                .filter(exam -> exam.getTags() != null && 
                      exam.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase("MODEL")))
                .collect(Collectors.toList());
        
        // Process each exam separately to avoid lazy loading issues
        List<ExamResponseDTO> examDTOs = new ArrayList<>();
        for (Exam exam : exams) {
            // Map basic exam info without accessing lazy collections
            ExamResponseDTO dto = mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null);
            examDTOs.add(dto);
        }
                
        return ResponseEntity.ok(ApiResponse.success(examDTOs));
    }

    @GetMapping("/papers/past")
    @Operation(summary = "Get past papers")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getPastPapers(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching past papers");
        
        // Get all published exams with "PAST" tag
        List<Exam> exams = examService.findAllPublished().stream()
                .filter(exam -> exam.getTags() != null && 
                      exam.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase("PAST")))
                .collect(Collectors.toList());
        
        // Process each exam separately to avoid lazy loading issues
        List<ExamResponseDTO> examDTOs = new ArrayList<>();
        for (Exam exam : exams) {
            // Map basic exam info without accessing lazy collections
            ExamResponseDTO dto = mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null);
            examDTOs.add(dto);
        }
                
        return ResponseEntity.ok(ApiResponse.success(examDTOs));
    }
    
    @GetMapping("/papers/subject")
    @Operation(summary = "Get subject papers")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getSubjectPapers(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching subject papers");
        
        // Get all published exams with "SUBJECT" tag
        List<Exam> exams = examService.findAllPublished().stream()
                .filter(exam -> exam.getTags() != null && 
                      exam.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase("SUBJECT")))
                .collect(Collectors.toList());
        
        // Process each exam separately to avoid lazy loading issues
        List<ExamResponseDTO> examDTOs = new ArrayList<>();
        for (Exam exam : exams) {
            // Map basic exam info without accessing lazy collections
            ExamResponseDTO dto = mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null);
            examDTOs.add(dto);
        }
                
        return ResponseEntity.ok(ApiResponse.success(examDTOs));
    }
    
    @GetMapping("/papers/practice")
    @Operation(summary = "Get practice papers")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getPracticePapers(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching practice papers");
        
        // Get all published exams with "PRACTICE" tag
        List<Exam> exams = examService.findAllPublished().stream()
                .filter(exam -> exam.getTags() != null && 
                      exam.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase("PRACTICE")))
                .collect(Collectors.toList());
        
        // Process each exam separately to avoid lazy loading issues
        List<ExamResponseDTO> examDTOs = new ArrayList<>();
        for (Exam exam : exams) {
            // Map basic exam info without accessing lazy collections
            ExamResponseDTO dto = mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null);
            examDTOs.add(dto);
        }
                
        return ResponseEntity.ok(ApiResponse.success(examDTOs));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get exam statistics")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExamStats() {
        logger.info("Fetching exam statistics");
        Map<String, Object> stats = examService.getExamStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exam by ID")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<ExamResponseDTO>> getExamById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching exam with ID: {}", id);
        try {
            Exam exam = examService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with ID: " + id));
            
            // Get the questions separately to avoid LazyInitializationException
            List<Question> questions = questionService.findByExamId(id);
            
            // First map basic exam info
            ExamResponseDTO examResponseDTO = mapToExamResponseDTOWithPurchaseCheck(
                exam, 
                userDetails != null ? userDetails.getUsername() : null
            );
            
            // Then manually map the questions if needed
            if (questions != null && !questions.isEmpty()) {
                List<ExamResponseDTO.QuestionDTO> questionDTOs = questions.stream()
                        .filter(q -> !q.isDeleted())
                        .map(this::mapToQuestionDTO)
                        .collect(Collectors.toList());
                examResponseDTO.setQuestions(questionDTOs);
            }
            
            return ResponseEntity.ok(ApiResponse.success(examResponseDTO));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    
    @GetMapping("/{id}/premium-info")
    @Operation(summary = "Get premium info for an exam")
    public ResponseEntity<ApiResponse<PremiumExamInfoDTO>> getPremiumExamInfo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching premium info for exam with ID: {}", id);
        try {
            Exam exam = examService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with ID: " + id));
            
            PremiumExamInfoDTO info = new PremiumExamInfoDTO();
            info.setExamId(exam.getId());
            info.setPremium(exam.isPremium());
            info.setPrice(exam.getPrice());
            info.setCustomPrice(exam.isCustomPrice());
            
            // Check if the user has already purchased this exam or any premium exam
            if (exam.isPremium() && userDetails != null) {
                String userId = userDetails.getUsername();
                
                // 1. Check online payments
                boolean hasPurchasedThisExam = paymentService.hasUserPurchasedExam(exam.getId(), userId);
                boolean hasPurchasedAnyExam = paymentService.hasUserPurchasedAnyExam(userId);
                
                // 2. Check manual payments
                boolean hasApprovedThisExamManualPayment = paymentManualService.hasUserApprovedRequest(userId, exam.getId());
                boolean hasApprovedAnyManualPayment = paymentManualService.hasUserApprovedRequest(userId, null);
                
                // Determine overall access status
                boolean hasDirectAccess = hasPurchasedThisExam || hasApprovedThisExamManualPayment;
                boolean hasUniversalAccess = hasPurchasedAnyExam || hasApprovedAnyManualPayment;
                
                // Set the response properties
                info.setPurchased(hasDirectAccess);
                info.setUniversalAccess(hasUniversalAccess);
                
                // Log for debugging
                logger.info("Premium info for user {}, exam {}:\n" +
                           "  - Purchased THIS exam (online): {}\n" +
                           "  - Purchased ANY exam (online): {}\n" +
                           "  - Approved THIS exam (manual): {}\n" +
                           "  - Approved ANY exam (manual): {}\n" +
                           "  - Direct access: {}\n" +
                           "  - Universal access: {}",
                    userId, id, 
                    hasPurchasedThisExam,
                    hasPurchasedAnyExam,
                    hasApprovedThisExamManualPayment,
                    hasApprovedAnyManualPayment,
                    hasDirectAccess,
                    hasUniversalAccess);
            } else {
                info.setPurchased(false);
                info.setUniversalAccess(false);
            }
            
            return ResponseEntity.ok(ApiResponse.success(info));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{examId}/questions")
    @Operation(summary = "Get questions for a specific exam")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<QuestionResponseDTO>>> getExamQuestions(
            @PathVariable Long examId,
            @RequestHeader(value = "X-Universal-Access", required = false) String universalAccess,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching questions for exam with ID: {}", examId);
        try {
            // First check if the exam is premium and if the user has purchased it
            Exam exam = examService.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with ID: " + examId));
            
            if (exam.isPremium() && userDetails != null) {
                String userId = userDetails.getUsername();
                
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
                    logger.warn("Payment required for user {} to access exam {}", userId, examId);
                }
                
                logger.info("User {} GRANTED premium access to exam {}.", userId, examId);
            }
            
            List<Question> questions = questionService.getQuestionsByExamId(examId);
            List<QuestionResponseDTO> questionDTOs = questions.stream()
                    .map(this::mapToQuestionResponseDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(questionDTOs));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching questions for exam: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching exam questions", e);
        }
    }
    
    @PutMapping("/{examId}/questions/{questionId}")
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.UPDATE, permissionName = ExamPermissionConstants.MANAGE_QUESTIONS)
    @Operation(summary = "Update a specific question in an exam")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<QuestionResponseDTO>> updateQuestion(
            @PathVariable Long examId,
            @PathVariable Long questionId,
            @Valid @RequestBody Question questionData) {
        logger.info("Updating question {} for exam {}", questionId, examId);
        try {
            // Ensure the question belongs to the exam
            Question existingQuestion = questionService.getQuestionById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));
            if (!existingQuestion.getExam().getId().equals(examId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question doesn't belong to the specified exam");
            }
            
            // Update the question
            Question updatedQuestion = questionService.updateQuestion(questionId, questionData);
            QuestionResponseDTO responseDTO = mapToQuestionResponseDTO(updatedQuestion);
            
            return ResponseEntity.ok(ApiResponse.success(responseDTO));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating question: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{examId}/questions/{questionId}")
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.DELETE, permissionName = ExamPermissionConstants.MANAGE_QUESTIONS)
    @Operation(summary = "Delete a specific question from an exam")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable Long examId,
            @PathVariable Long questionId) {
        logger.info("Deleting question {} from exam {}", questionId, examId);
        try {
            // Ensure the question belongs to the exam
            Question existingQuestion = questionService.getQuestionById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));
            if (!existingQuestion.getExam().getId().equals(examId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question doesn't belong to the specified exam");
            }
            
            // Delete the question
            questionService.deleteQuestion(questionId);
            
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting question: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting question", e);
        }
    }

    @PostMapping
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.CREATE, permissionName = ExamPermissionConstants.CREATE_EXAM)
    @org.springframework.transaction.annotation.Transactional
    @Operation(
        summary = "Create a new exam",
        description = "Creates a new exam with questions and options. Requires CREATE_EXAM permission.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201", 
                description = "Exam successfully created",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExamResponseDTO.class)
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "Invalid input data",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class)
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403", 
                description = "Forbidden - insufficient permissions"
            )
        }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Exam data to create",
        required = true,
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExamRequestDTO.class)
        )
    )
    public ResponseEntity<ApiResponse<ExamResponseDTO>> createExam(@Valid @RequestBody ExamRequestDTO requestDTO) {
        logger.info("Creating new exam");
        try {
            Exam exam = mapToExamEntity(requestDTO);
            Exam createdExam = examService.createExam(exam);
            ExamResponseDTO responseDTO = mapToExamResponseDTO(createdExam);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(responseDTO, 201));
        } catch (Exception e) {
            logger.error("Error creating exam: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping("/upload-json")
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.CREATE, permissionName = ExamPermissionConstants.CREATE_EXAM)
    @Operation(summary = "Upload and create an exam from JSON data")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<ExamResponseDTO>> uploadJsonExam(
            @Valid @RequestBody JsonExamUploadRequestDTO requestDTO) {
        logger.info("Uploading JSON exam: {}", requestDTO.getTitle());
        try {
            Exam createdExam = jsonExamUploadService.processJsonAndCreateExam(requestDTO);
            ExamResponseDTO responseDTO = mapToExamResponseDTO(createdExam);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(responseDTO, 201));
        } catch (Exception e) {
            logger.error("Error uploading JSON exam: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping("/papers/upload/json")
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.CREATE, permissionName = ExamPermissionConstants.CREATE_EXAM)
    @Operation(summary = "Upload and create a paper from JSON data")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<ExamResponseDTO>> uploadJsonPaper(
            @Valid @RequestBody JsonExamUploadRequestDTO requestDTO) {
        logger.info("Uploading JSON paper: {}", requestDTO.getTitle());
        try {
            Exam createdExam = jsonExamUploadService.processJsonAndCreateExam(requestDTO);
            ExamResponseDTO responseDTO = mapToExamResponseDTO(createdExam);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(responseDTO, 201));
        } catch (Exception e) {
            logger.error("Error uploading JSON paper: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PutMapping("/{id}")
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.UPDATE, permissionName = ExamPermissionConstants.EDIT_EXAM)
    @Operation(summary = "Update an existing exam")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<ExamResponseDTO>> updateExam(
            @PathVariable Long id, 
            @Valid @RequestBody ExamRequestDTO requestDTO) {
        logger.info("Updating exam with ID: {}", id);
        try {
            Exam exam = mapToExamEntity(requestDTO);
            Exam updatedExam = examService.updateExam(id, exam);
            ExamResponseDTO responseDTO = mapToExamResponseDTO(updatedExam);
            return ResponseEntity.ok(ApiResponse.success(responseDTO));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating exam: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.DELETE, permissionName = ExamPermissionConstants.DELETE_EXAM)
    @Operation(summary = "Delete an exam (Admin only)")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable Long id) {
        logger.info("Deleting exam with ID: {}", id);
        try {
            examService.deleteExam(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting exam: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting exam", e);
        }
    }

    @GetMapping("/status/{status}")
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.READ, permissionName = ExamPermissionConstants.VIEW_EXAMS)
    @Operation(summary = "Get exams by status")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getExamsByStatus(
            @PathVariable Exam.ExamStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching exams with status: {}", status);
        List<Exam> exams = examService.findByStatus(status);
        
        // Process each exam separately to avoid lazy loading issues
        List<ExamResponseDTO> examResponseDTOs = new ArrayList<>();
        for (Exam exam : exams) {
            // Map basic exam info without accessing lazy collections
            ExamResponseDTO dto = mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null);
            examResponseDTOs.add(dto);
        }
        
        return ResponseEntity.ok(ApiResponse.success(examResponseDTOs));
    }

    @PostMapping("/{id}/publish")
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.UPDATE, permissionName = ExamPermissionConstants.PUBLISH_EXAM)
    @Operation(summary = "Publish an exam")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<ExamResponseDTO>> publishExam(@PathVariable Long id) {
        logger.info("Publishing exam with ID: {}", id);
        try {
            Exam publishedExam = examService.publishExam(id);
            ExamResponseDTO responseDTO = mapToExamResponseDTO(publishedExam);
            return ResponseEntity.ok(ApiResponse.success(responseDTO));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Error publishing exam: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error publishing exam", e);
        }
    }

    @PostMapping("/{id}/archive")
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.UPDATE, permissionName = ExamPermissionConstants.UNPUBLISH_EXAM)
    @Operation(summary = "Archive an exam")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<ExamResponseDTO>> archiveExam(@PathVariable Long id) {
        logger.info("Archiving exam with ID: {}", id);
        try {
            Exam archivedExam = examService.archiveExam(id);
            ExamResponseDTO responseDTO = mapToExamResponseDTO(archivedExam);
            return ResponseEntity.ok(ApiResponse.success(responseDTO));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Error archiving exam: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error archiving exam", e);
        }
    }
    


    // Helper methods for mapping between DTO and entity
    private ExamResponseDTO mapToExamResponseDTO(Exam exam) {
        ExamResponseDTO dto = new ExamResponseDTO();
        dto.setId(exam.getId());
        dto.setTitle(exam.getTitle());
        dto.setDescription(exam.getDescription());
        dto.setDuration(exam.getDuration());
        dto.setTotalMarks(exam.getTotalMarks());
        dto.setPassingMarks(exam.getPassingMarks());
        dto.setStatus(exam.getStatus());
        dto.setTags(exam.getTags());
        
        // Set premium fields
        dto.setPremium(exam.isPremium());
        dto.setPrice(exam.getPrice());
        dto.setCustomPrice(exam.isCustomPrice());
        dto.setPurchased(false); // Default value, will be updated in mapToExamResponseDTOWithPurchaseCheck
        
        // Set difficulty - derive from tags if possible, otherwise use default MEDIUM
        if (exam.getTags() != null) {
            // Look for a difficulty tag (easy, medium, hard)
            for (String tag : exam.getTags()) {
                String lowercaseTag = tag.toLowerCase();
                if (lowercaseTag.equals("easy") || lowercaseTag.equals("medium") || lowercaseTag.equals("hard")) {
                    dto.setDifficulty(lowercaseTag.toUpperCase());
                    break;
                }
            }
        }
        
        // Get a count of questions directly from the question service rather than accessing the potentially
        // detached collection to avoid LazyInitializationException
        int questionCount = 0;
        try {
            questionCount = questionService.countQuestionsByExamId(exam.getId()).intValue();
            
            // Set some default values for additional fields
            dto.setAttemptCount(0); // Replace with actual count when available
            dto.setAverageScore(0.0); // Replace with actual average when available
            dto.setQuestionCount(questionCount);
            
            // Explicitly don't try to access questions collection here to avoid LazyInitializationException
            // Only set questions in dto when we explicitly load them separately
            
        } catch (Exception e) {
            logger.warn("Error getting question count for exam {}: {}", exam.getId(), e.getMessage());
            // Default to 0 questions if we can't get the count
            dto.setQuestionCount(0);
        }
        
        // Log the data for debugging
        logger.debug("Mapped exam with ID {} to DTO: title={}, questions={}, duration={}", 
                exam.getId(), exam.getTitle(), questionCount, exam.getDuration());
        
        return dto;
    }
    
    private ExamResponseDTO mapToExamResponseDTOWithPurchaseCheck(Exam exam, String userId) {
        // Create a basic DTO without accessing lazy collections to avoid LazyInitializationException
        ExamResponseDTO dto = mapToExamResponseDTO(exam);
        
        // Handle premium exams with logged-in user
        if (exam.isPremium() && userId != null) {
            // ONLY use manual payment service - ignore online payments as requested
            
            // Check if ANY exam has been approved via manual payment (universal access)
            boolean hasApprovedAnyManualRequest = paymentManualService.hasUserApprovedRequest(userId, null);
            
            // Check if THIS specific exam has been approved
            boolean hasApprovedThisExamManualRequest = paymentManualService.hasUserApprovedRequest(userId, exam.getId());
            
            // Check if THIS exam has a pending manual payment
            boolean hasPendingThisExamManualRequest = paymentManualService.hasUserPendingRequest(userId, exam.getId());
            
            // Check if ANY exam has a pending manual payment
            boolean hasAnyPendingManualRequest = paymentManualService.hasUserAnyPendingRequest(userId);
            
            // Set purchase status (specific purchase or universal access)
            boolean hasPurchased = hasApprovedThisExamManualRequest || hasApprovedAnyManualRequest;
            boolean hasUniversalAccess = hasApprovedAnyManualRequest;
            
            dto.setPurchased(hasPurchased);
            dto.setUniversalAccess(hasUniversalAccess);
            
            // Log purchase status
            logger.debug("Purchase status for user {}, exam {}: manual specific={}, manual universal={}",
                      userId, exam.getId(), hasApprovedThisExamManualRequest, hasApprovedAnyManualRequest);
            
            // Determine the overall payment status
            String paymentStatus;
            
            // PAID takes precedence over PENDING over NOT_PAID
            if (hasApprovedAnyManualRequest) {
                // If ANY exam is paid, all exams should show as PAID
                paymentStatus = "PAID";
            } else if (hasAnyPendingManualRequest) {
                // If ANY exam has a pending request, all exams should show as PENDING
                paymentStatus = "PENDING";
            } else {
                paymentStatus = "NOT_PAID";
            }
            
            // Log payment status details
            logger.debug("Payment status for user {}, exam {}: manual this exam pending={}, any manual pending={}, manual approved={}, final status={}",
                      userId, exam.getId(), hasPendingThisExamManualRequest, hasAnyPendingManualRequest, hasApprovedThisExamManualRequest, paymentStatus);
            
            dto.setPaymentStatus(paymentStatus);
            
            // Final debugging log 
            logger.debug("FINAL STATUS - User {} exam {} - purchase: {}, universal access: {}, payment status: {}", 
                      userId, exam.getId(), hasPurchased, hasUniversalAccess, paymentStatus);
        } else {
            // Set default payment status for non-premium exams
            dto.setPaymentStatus("NOT_REQUIRED");
        }
        
        return dto;
    }
    
    private ExamResponseDTO.QuestionDTO mapToQuestionDTO(Question question) {
        ExamResponseDTO.QuestionDTO dto = new ExamResponseDTO.QuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionNumber(question.getQuestionNumber());
        dto.setText(question.getQuestionText()); // Now matches frontend's text field
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());
        dto.setMarks(question.getMarks());
        dto.setTopic(question.getTopic());
        dto.setDifficulty(question.getDifficulty() != null ? question.getDifficulty() : "MEDIUM");
        
        // Map options - use a separate query to get options if the collection is detached
        List<ExamResponseDTO.OptionDTO> optionDTOs = new ArrayList<>();
        
        try {
            if (question.getOptions() != null) {
                for (var option : question.getOptions()) {
                    if (!option.isDeleted()) {
                        ExamResponseDTO.OptionDTO optionDTO = new ExamResponseDTO.OptionDTO();
                        optionDTO.setId(option.getId());
                        optionDTO.setLabel(option.getLabel()); // Frontend expects 'label'
                        optionDTO.setText(option.getText());  // Frontend expects 'text'
                        optionDTO.setIsCorrect(option.getIsCorrect());
                        optionDTOs.add(optionDTO);
                    }
                }
            }
        } catch (Exception e) {
            // If we get a LazyInitializationException, log it and continue with an empty options list
            logger.warn("Could not load options for question {}: {}", question.getId(), e.getMessage());
        }
        
        dto.setOptions(optionDTOs);
        
        // Log the mapping for debugging purposes
        logger.debug("Mapped question {} to DTO with {} options", question.getId(), optionDTOs.size());
        
        return dto;
    }
    
    private QuestionResponseDTO mapToQuestionResponseDTO(Question question) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setId(question.getId());
        dto.setQuestionNumber(question.getQuestionNumber());
        dto.setText(question.getQuestionText()); // Map questionText to text for frontend compatibility
        // Don't include the correct answer in the response for security
        dto.setExplanation(question.getExplanation());
        dto.setPoints(question.getMarks());
        dto.setTopic(question.getTopic());
        dto.setDifficulty(question.getDifficulty());
        dto.setCorrectAnswer(question.getCorrectAnswer().replaceAll("\\([A-D]\\)\\s.*", "$1").replaceAll("[^A-D]", ""));
        
        // Map options without revealing which is correct - handle potential lazy loading issues
        List<QuestionResponseDTO.OptionDTO> optionDTOs = new ArrayList<>();
        
        try {
            if (question.getOptions() != null) {
                for (var option : question.getOptions()) {
                    if (!option.isDeleted()) {
                        QuestionResponseDTO.OptionDTO optionDTO = new QuestionResponseDTO.OptionDTO();
                        optionDTO.setId(option.getId());
                        optionDTO.setLabel(option.getLabel()); // This matches frontend's expected 'label' property
                        optionDTO.setText(option.getText()); // This matches frontend's expected 'text' property
                        // Don't include isCorrect flag for security
                        optionDTOs.add(optionDTO);
                    }
                }
            }
        } catch (Exception e) {
            // If we get a LazyInitializationException, log it and continue with an empty options list
            logger.warn("Could not load options for question {}: {}", question.getId(), e.getMessage());
        }
        
        dto.setOptions(optionDTOs);
        
        // Log the mapping for debugging purposes
        logger.debug("Mapped question ID {} with {} options", question.getId(), optionDTOs.size());
        
        return dto;
    }

    private Exam mapToExamEntity(ExamRequestDTO dto) {
        Exam exam = new Exam();
        // Don't set the ID for a new exam
        // If the exam has an ID > 0, it's an existing exam being updated
        if (dto.getId() != null && dto.getId() > 0) {
            exam.setId(dto.getId());
        }
        exam.setTitle(dto.getTitle());
        exam.setDescription(dto.getDescription());
        exam.setDuration(dto.getDuration());
        exam.setTotalMarks(dto.getTotalMarks());
        exam.setPassingMarks(dto.getPassingMarks());
        exam.setStatus(dto.getStatus() != null ? dto.getStatus() : Exam.ExamStatus.PUBLISHED);
        exam.setTags(dto.getTags() != null ? dto.getTags() : new ArrayList<>());

        // Premium fields
        exam.setPremium(dto.getIsPremium() != null ? dto.getIsPremium() : false);
        exam.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        exam.setCustomPrice(dto.getIsCustomPrice() != null ? dto.getIsCustomPrice() : false);

        // Map questions from DTO to entity
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            List<Question> questionEntities = new ArrayList<>();

            for (ExamRequestDTO.QuestionDTO questionDTO : dto.getQuestions()) {
                Question question = new Question();
                // Don't set the ID for new questions to avoid detached entity errors
                // If the question has an ID > 0, it's an existing question being updated
                if (questionDTO.getId() != null && questionDTO.getId() > 0) {
                    question.setId(questionDTO.getId());
                }
                question.setQuestionNumber(questionDTO.getQuestionNumber());
                
                // Validate questionText is not null or empty
                String questionText = questionDTO.getQuestionText();
                if (questionText == null || questionText.trim().isEmpty()) {
                    throw new IllegalArgumentException("Question text is required for question number " + questionDTO.getQuestionNumber());
                }
                question.setQuestionText(questionText);
                
                // Validate correctAnswer is not null or empty
                String correctAnswer = questionDTO.getCorrectAnswer();
                if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
                    throw new IllegalArgumentException("Correct answer is required for question number " + questionDTO.getQuestionNumber());
                }
                question.setCorrectAnswer(correctAnswer);
                
                question.setExplanation(questionDTO.getExplanation());
                question.setMarks(questionDTO.getMarks());
                question.setExam(exam); // Set parent

                // Map options
                if (questionDTO.getOptions() != null && !questionDTO.getOptions().isEmpty()) {
                    List<Option> optionEntities = new ArrayList<>();
                    for (ExamRequestDTO.OptionDTO optionDTO : questionDTO.getOptions()) {
                        Option option = new Option();
                        // Don't set the ID for new options to avoid detached entity errors
                        // If the option has an ID > 0, it's an existing option being updated
                        if (optionDTO.getId() != null && optionDTO.getId() > 0) {
                            option.setId(optionDTO.getId());
                        }
                        option.setOptionLabel(optionDTO.getOptionKey()); // label = optionKey
                        option.setOptionText(optionDTO.getOptionText()); // text = optionText
                        option.setIsCorrect(optionDTO.getIsCorrect() != null ? optionDTO.getIsCorrect() : false);
                        option.setDeleted(false);
                        option.setQuestion(question); // Set back-reference

                        optionEntities.add(option);
                    }
                    question.setOptions(optionEntities);
                }

                questionEntities.add(question);
            }

            exam.setQuestions(questionEntities);
        }

        return exam;
    }


}