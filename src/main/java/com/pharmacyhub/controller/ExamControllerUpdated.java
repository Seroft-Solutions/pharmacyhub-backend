package com.pharmacyhub.controller;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.domain.entity.Question;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.request.ExamRequestDTO;
import com.pharmacyhub.dto.request.JsonExamUploadRequestDTO;
import com.pharmacyhub.dto.response.ExamResponseDTO;
import com.pharmacyhub.dto.response.QuestionResponseDTO;
import com.pharmacyhub.payment.dto.PremiumExamInfoDTO;
import com.pharmacyhub.payment.exception.PaymentRequiredException;
import com.pharmacyhub.payment.service.PaymentService;
import com.pharmacyhub.security.annotation.RequiresPermission;
import com.pharmacyhub.security.constants.ExamPermissionConstants;
import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.service.ExamAttemptService;
import com.pharmacyhub.service.ExamService;
import com.pharmacyhub.service.JsonExamUploadService;
import com.pharmacyhub.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/exams")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Exams", description = "API endpoints for exam management")
public class ExamControllerUpdated {

    private static final Logger logger = LoggerFactory.getLogger(ExamControllerUpdated.class);

    private final ExamService examService;
    private final QuestionService questionService;
    private final JsonExamUploadService jsonExamUploadService;
    private final PaymentService paymentService;
    private final ExamAttemptService examAttemptService;

    @Autowired
    public ExamControllerUpdated(
            ExamService examService, 
            QuestionService questionService, 
            JsonExamUploadService jsonExamUploadService,
            PaymentService paymentService,
            ExamAttemptService examAttemptService) {
        this.examService = examService;
        this.questionService = questionService;
        this.jsonExamUploadService = jsonExamUploadService;
        this.paymentService = paymentService;
        this.examAttemptService = examAttemptService;
    }

    @GetMapping
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.READ, permissionName = ExamPermissionConstants.VIEW_EXAMS)
    @Operation(summary = "Get all exams - Admin/Instructor only")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getAllExams(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching all exams");
        List<Exam> exams = examService.findAllActive();
        List<ExamResponseDTO> examResponseDTOs = exams.stream()
                .map(exam -> mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(examResponseDTOs));
    }

    @GetMapping("/published")
    @Operation(summary = "Get all published exams - Public access")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getAllPublishedExams(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching all published exams");
        try {
            List<Exam> publishedExams = examService.findAllPublished();
            List<ExamResponseDTO> examResponseDTOs = publishedExams.stream()
                    .map(exam -> mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null))
                    .collect(Collectors.toList());
            logger.info("Successfully fetched {} published exams", examResponseDTOs.size());
            return ResponseEntity.ok(ApiResponse.success(examResponseDTOs));
        } catch (Exception e) {
            logger.error("Error fetching published exams: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching published exams", e);
        }
    }
    
    @GetMapping("/papers/model")
    @Operation(summary = "Get model papers")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getModelPapers(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching model papers");
        
        // Get all published exams with "MODEL" tag
        List<Exam> exams = examService.findAllPublished().stream()
                .filter(exam -> exam.getTags() != null && 
                      exam.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase("MODEL")))
                .collect(Collectors.toList());
        
        List<ExamResponseDTO> examDTOs = exams.stream()
                .map(exam -> mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(ApiResponse.success(examDTOs));
    }

    @GetMapping("/papers/past")
    @Operation(summary = "Get past papers")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getPastPapers(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching past papers");
        
        // Get all published exams with "PAST" tag
        List<Exam> exams = examService.findAllPublished().stream()
                .filter(exam -> exam.getTags() != null && 
                      exam.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase("PAST")))
                .collect(Collectors.toList());
        
        List<ExamResponseDTO> examDTOs = exams.stream()
                .map(exam -> mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(ApiResponse.success(examDTOs));
    }
    
    @GetMapping("/papers/subject")
    @Operation(summary = "Get subject papers")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getSubjectPapers(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching subject papers");
        
        // Get all published exams with "SUBJECT" tag
        List<Exam> exams = examService.findAllPublished().stream()
                .filter(exam -> exam.getTags() != null && 
                      exam.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase("SUBJECT")))
                .collect(Collectors.toList());
        
        List<ExamResponseDTO> examDTOs = exams.stream()
                .map(exam -> mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(ApiResponse.success(examDTOs));
    }
    
    @GetMapping("/papers/practice")
    @Operation(summary = "Get practice papers")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getPracticePapers(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching practice papers");
        
        // Get all published exams with "PRACTICE" tag
        List<Exam> exams = examService.findAllPublished().stream()
                .filter(exam -> exam.getTags() != null && 
                      exam.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase("PRACTICE")))
                .collect(Collectors.toList());
        
        List<ExamResponseDTO> examDTOs = exams.stream()
                .map(exam -> mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(ApiResponse.success(examDTOs));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get exam statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExamStats() {
        logger.info("Fetching exam statistics");
        Map<String, Object> stats = examService.getExamStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exam by ID")
    public ResponseEntity<ApiResponse<ExamResponseDTO>> getExamById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching exam with ID: {}", id);
        try {
            Exam exam = examService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with ID: " + id));
            
            ExamResponseDTO examResponseDTO = mapToExamResponseDTOWithPurchaseCheck(
                exam, 
                userDetails != null ? userDetails.getUsername() : null
            );
            
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
            
            // Check if the user has already purchased this exam
            if (exam.isPremium() && userDetails != null) {
                String userId = userDetails.getUsername();
                boolean hasPurchased = paymentService.hasUserPurchasedExam(exam.getId(), userId);
                info.setPurchased(hasPurchased);
            } else {
                info.setPurchased(false);
            }
            
            return ResponseEntity.ok(ApiResponse.success(info));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{examId}/questions")
    @Operation(summary = "Get questions for a specific exam")
    public ResponseEntity<ApiResponse<List<QuestionResponseDTO>>> getExamQuestions(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching questions for exam with ID: {}", examId);
        try {
            // First check if the exam is premium and if the user has purchased it
            Exam exam = examService.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with ID: " + examId));
            
            if (exam.isPremium() && userDetails != null) {
                String userId = userDetails.getUsername();
                boolean hasPurchased = paymentService.hasUserPurchasedExam(examId, userId);
                
                if (!hasPurchased) {
                    throw new PaymentRequiredException("Payment required to access questions for this premium exam");
                }
            }
            
            List<Question> questions = questionService.getQuestionsByExamId(examId);
            List<QuestionResponseDTO> questionDTOs = questions.stream()
                    .map(this::mapToQuestionResponseDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(questionDTOs));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (PaymentRequiredException e) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching questions for exam: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching exam questions", e);
        }
    }
    
    @PutMapping("/{examId}/questions/{questionId}")
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.UPDATE, permissionName = ExamPermissionConstants.MANAGE_QUESTIONS)
    @Operation(summary = "Update a specific question in an exam")
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
    @Operation(summary = "Create a new exam")
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

    @PutMapping("/{id}")
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.UPDATE, permissionName = ExamPermissionConstants.EDIT_EXAM)
    @Operation(summary = "Update an existing exam")
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
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getExamsByStatus(
            @PathVariable Exam.ExamStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching exams with status: {}", status);
        List<Exam> exams = examService.findByStatus(status);
        List<ExamResponseDTO> examResponseDTOs = exams.stream()
                .map(exam -> mapToExamResponseDTOWithPurchaseCheck(exam, userDetails != null ? userDetails.getUsername() : null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(examResponseDTOs));
    }

    @PostMapping("/{id}/publish")
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.UPDATE, permissionName = ExamPermissionConstants.PUBLISH_EXAM)
    @Operation(summary = "Publish an exam")
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
    
    @PostMapping("/{id}/start")
    @Operation(summary = "Start an exam attempt")
    public ResponseEntity<ApiResponse<Object>> startExam(
            @PathVariable("id") Long examId,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        // Get exam details
        Exam exam = examService.findById(examId)
            .orElseThrow(() -> new EntityNotFoundException("Exam not found with id: " + examId));
            
        // Check if the exam is premium
        if (exam.isPremium()) {
            String userId = userDetails.getUsername();
            
            // Check if the user has purchased this exam
            boolean hasPurchased = paymentService.hasUserPurchasedExam(examId, userId);
            
            if (!hasPurchased) {
                throw new PaymentRequiredException("Payment required to access this premium exam");
            }
        }
        
        // Continue with the existing start exam logic
        String userId = userDetails.getUsername();
        try {
            var attempt = examAttemptService.startExam(examId, userId);
            return ResponseEntity.ok(ApiResponse.success(attempt));
        } catch (Exception e) {
            logger.error("Error starting exam: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
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
        
        // Map questions if present (but don't include them for list operations)
        if (exam.getQuestions() != null && !exam.getQuestions().isEmpty()) {
            List<ExamResponseDTO.QuestionDTO> questionDTOs = exam.getQuestions().stream()
                    .filter(q -> !q.isDeleted())
                    .map(this::mapToQuestionDTO)
                    .collect(Collectors.toList());
            dto.setQuestions(questionDTOs);
        }
        
        // Set additional fields that might be used by frontend
        dto.setAttemptCount(0); // Replace with actual count when available
        dto.setAverageScore(0.0); // Replace with actual average when available
        
        // Calculate the number of questions - this is crucial for the frontend
        // Don't rely on getQuestionCount() method to calculate this explicitly
        int questionCount = 0;
        if (exam.getQuestions() != null) {
            questionCount = (int) exam.getQuestions().stream()
                    .filter(q -> !q.isDeleted())
                    .count();
        }
        dto.setQuestionCount(questionCount);
        
        // Log the data for debugging
        logger.debug("Mapped exam with ID {} to DTO: title={}, questions={}, duration={}", 
                exam.getId(), exam.getTitle(), questionCount, exam.getDuration());
        
        return dto;
    }
    
    private ExamResponseDTO mapToExamResponseDTOWithPurchaseCheck(Exam exam, String userId) {
        ExamResponseDTO dto = mapToExamResponseDTO(exam);
        
        // Check if the user has purchased this exam if it's premium
        if (exam.isPremium() && userId != null) {
            boolean hasPurchased = paymentService.hasUserPurchasedExam(exam.getId(), userId);
            dto.setPurchased(hasPurchased);
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
        
        // Map options
        if (question.getOptions() != null) {
            List<ExamResponseDTO.OptionDTO> optionDTOs = question.getOptions().stream()
                    .filter(option -> !option.isDeleted())
                    .map(option -> {
                        ExamResponseDTO.OptionDTO optionDTO = new ExamResponseDTO.OptionDTO();
                        optionDTO.setId(option.getId());
                        optionDTO.setLabel(option.getLabel()); // Frontend expects 'label'
                        optionDTO.setText(option.getText());  // Frontend expects 'text'
                        optionDTO.setIsCorrect(option.getIsCorrect());
                        return optionDTO;
                    })
                    .collect(Collectors.toList());
            dto.setOptions(optionDTOs);
        }
        
        // Log the mapping for debugging purposes
        logger.debug("Mapped question {} to DTO with {} options", question.getId(), 
                (question.getOptions() != null ? question.getOptions().size() : 0));
        
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
        // Map options without revealing which is correct
        if (question.getOptions() != null) {
            List<QuestionResponseDTO.OptionDTO> optionDTOs = question.getOptions().stream()
                    .filter(option -> !option.isDeleted())
                    .map(option -> {
                        QuestionResponseDTO.OptionDTO optionDTO = new QuestionResponseDTO.OptionDTO();
                        optionDTO.setId(option.getId());
                        optionDTO.setLabel(option.getLabel()); // This matches frontend's expected 'label' property
                        optionDTO.setText(option.getText()); // This matches frontend's expected 'text' property
                        // Don't include isCorrect flag for security
                        return optionDTO;
                    })
                    .collect(Collectors.toList());
            dto.setOptions(optionDTOs);
        }
        
        // Log the mapping for debugging purposes
        logger.debug("Mapped question ID {} with {} options", 
            question.getId(), 
            question.getOptions() != null ? question.getOptions().size() : 0);
        
        return dto;
    }
    
    private Exam mapToExamEntity(ExamRequestDTO dto) {
        Exam exam = new Exam();
        exam.setTitle(dto.getTitle());
        exam.setDescription(dto.getDescription());
        exam.setDuration(dto.getDuration());
        exam.setTotalMarks(dto.getTotalMarks());
        exam.setPassingMarks(dto.getPassingMarks());
        exam.setStatus(dto.getStatus() != null ? dto.getStatus() : Exam.ExamStatus.DRAFT);
        exam.setTags(dto.getTags());
        
        // Set premium fields
        exam.setPremium(dto.getIsPremium() != null ? dto.getIsPremium() : false);
        exam.setPrice(dto.getPrice());
        
        // Questions will be added/updated separately
        return exam;
    }
}