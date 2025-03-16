package com.pharmacyhub.controller;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.domain.entity.Question;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.request.ExamRequestDTO;
import com.pharmacyhub.dto.request.JsonExamUploadRequestDTO;
import com.pharmacyhub.dto.response.ExamResponseDTO;
import com.pharmacyhub.dto.response.QuestionResponseDTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/exams")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Exams", description = "API endpoints for exam management")
public class ExamController {

    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);

    private final ExamService examService;
    private final QuestionService questionService;
    private final JsonExamUploadService jsonExamUploadService;

    public ExamController(ExamService examService, QuestionService questionService, JsonExamUploadService jsonExamUploadService) {
        this.examService = examService;
        this.questionService = questionService;
        this.jsonExamUploadService = jsonExamUploadService;
    }

    @GetMapping
    @RequiresPermission(resource = ResourceType.PHARMACY, operation = OperationType.READ, permissionName = ExamPermissionConstants.VIEW_EXAMS)
    @Operation(summary = "Get all exams - Admin/Instructor only")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getAllExams() {
        logger.info("Fetching all exams");
        List<Exam> exams = examService.findAllActive();
        List<ExamResponseDTO> examResponseDTOs = exams.stream()
                .map(this::mapToExamResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(examResponseDTOs));
    }

    @GetMapping("/published")
    @Operation(summary = "Get all published exams - Public access")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getAllPublishedExams() {
        logger.info("Fetching all published exams");
        try {
            List<Exam> publishedExams = examService.findAllPublished();
            List<ExamResponseDTO> examResponseDTOs = publishedExams.stream()
                    .map(this::mapToExamResponseDTO)
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
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getModelPapers() {
        logger.info("Fetching model papers");
        
        // Get all published exams with "MODEL" tag
        List<Exam> exams = examService.findAllPublished().stream()
                .filter(exam -> exam.getTags() != null && 
                      exam.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase("MODEL")))
                .collect(Collectors.toList());
        
        List<ExamResponseDTO> examDTOs = exams.stream()
                .map(this::mapToExamResponseDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(ApiResponse.success(examDTOs));
    }

    @GetMapping("/papers/past")
    @Operation(summary = "Get past papers")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getPastPapers() {
        logger.info("Fetching past papers");
        
        // Get all published exams with "PAST" tag
        List<Exam> exams = examService.findAllPublished().stream()
                .filter(exam -> exam.getTags() != null && 
                      exam.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase("PAST")))
                .collect(Collectors.toList());
        
        List<ExamResponseDTO> examDTOs = exams.stream()
                .map(this::mapToExamResponseDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(ApiResponse.success(examDTOs));
    }
    
    @GetMapping("/papers/subject")
    @Operation(summary = "Get subject papers")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getSubjectPapers() {
        logger.info("Fetching subject papers");
        
        // Get all published exams with "SUBJECT" tag
        List<Exam> exams = examService.findAllPublished().stream()
                .filter(exam -> exam.getTags() != null && 
                      exam.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase("SUBJECT")))
                .collect(Collectors.toList());
        
        List<ExamResponseDTO> examDTOs = exams.stream()
                .map(this::mapToExamResponseDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(ApiResponse.success(examDTOs));
    }
    
    @GetMapping("/papers/practice")
    @Operation(summary = "Get practice papers")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getPracticePapers() {
        logger.info("Fetching practice papers");
        
        // Get all published exams with "PRACTICE" tag
        List<Exam> exams = examService.findAllPublished().stream()
                .filter(exam -> exam.getTags() != null && 
                      exam.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase("PRACTICE")))
                .collect(Collectors.toList());
        
        List<ExamResponseDTO> examDTOs = exams.stream()
                .map(this::mapToExamResponseDTO)
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
    public ResponseEntity<ApiResponse<ExamResponseDTO>> getExamById(@PathVariable Long id) {
        logger.info("Fetching exam with ID: {}", id);
        try {
            Exam exam = examService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with ID: " + id));
            ExamResponseDTO examResponseDTO = mapToExamResponseDTO(exam);
            return ResponseEntity.ok(ApiResponse.success(examResponseDTO));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{examId}/questions")
    @Operation(summary = "Get questions for a specific exam")
    public ResponseEntity<ApiResponse<List<QuestionResponseDTO>>> getExamQuestions(@PathVariable Long examId) {
        logger.info("Fetching questions for exam with ID: {}", examId);
        try {
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
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getExamsByStatus(@PathVariable Exam.ExamStatus status) {
        logger.info("Fetching exams with status: {}", status);
        List<Exam> exams = examService.findByStatus(status);
        List<ExamResponseDTO> examResponseDTOs = exams.stream()
                .map(this::mapToExamResponseDTO)
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
        
        // Map questions if present (but don't include them for list operations)
        if (exam.getQuestions() != null && !exam.getQuestions().isEmpty()) {
            List<ExamResponseDTO.QuestionDTO> questionDTOs = exam.getQuestions().stream()
                    .map(this::mapToQuestionDTO)
                    .collect(Collectors.toList());
            dto.setQuestions(questionDTOs);
        }
        
        return dto;
    }
    
    private ExamResponseDTO.QuestionDTO mapToQuestionDTO(Question question) {
        ExamResponseDTO.QuestionDTO dto = new ExamResponseDTO.QuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionNumber(question.getQuestionNumber());
        dto.setQuestionText(question.getQuestionText());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());
        dto.setMarks(question.getMarks());
        dto.setTopic(question.getTopic());
        dto.setDifficulty(question.getDifficulty());
        
        // Map options
        if (question.getOptions() != null) {
            List<ExamResponseDTO.OptionDTO> optionDTOs = question.getOptions().stream()
                    .map(option -> {
                        ExamResponseDTO.OptionDTO optionDTO = new ExamResponseDTO.OptionDTO();
                        optionDTO.setId(option.getId());
                        optionDTO.setOptionKey(option.getLabel());
                        optionDTO.setOptionText(option.getText());
                        optionDTO.setIsCorrect(option.getIsCorrect());
                        return optionDTO;
                    })
                    .collect(Collectors.toList());
            dto.setOptions(optionDTOs);
        }
        
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
        
        // Map options without revealing which is correct
        if (question.getOptions() != null) {
            List<QuestionResponseDTO.OptionDTO> optionDTOs = question.getOptions().stream()
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
        
        // Questions will be added/updated separately
        return exam;
    }
}