package com.pharmacyhub.controller;

import com.pharmacyhub.controller.base.BaseController;
import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.domain.entity.Option;
import com.pharmacyhub.domain.entity.Question;
import com.pharmacyhub.dto.request.ExamRequestDTO;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.response.ApiError;
import com.pharmacyhub.dto.response.ExamResponseDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Exams", description = "API endpoints for exam management")
public class ExamController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Get all exams - Admin/Instructor only")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getAllExams() {
        List<Exam> exams = examService.findAllActive();
        List<ExamResponseDTO> examResponseDTOs = mapToDTO(exams, ExamResponseDTO.class);
        return successResponse(examResponseDTOs);
    }

    /**
     * Get published exams - publicly accessible without authentication
     */
    @GetMapping("/published")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get all published exams - Public access")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getAllPublishedExams() {
        logger.info("Fetching all published exams");
        try {
            List<Exam> publishedExams = examService.findAllPublished();
            List<ExamResponseDTO> examResponseDTOs = mapToDTO(publishedExams, ExamResponseDTO.class);
            logger.info("Successfully fetched {} published exams", examResponseDTOs.size());
            return successResponse(examResponseDTOs);
        } catch (Exception e) {
            logger.error("Error fetching published exams: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR') or @examAccessEvaluator.canAccessExam(authentication, #id)")
    @Operation(summary = "Get exam by ID")
    public ResponseEntity<ApiResponse<ExamResponseDTO>> getExamById(@PathVariable Long id) {
        return examService.findById(id)
                .map(exam -> {
                    ExamResponseDTO examResponseDTO = mapToDTO(exam, ExamResponseDTO.class);
                    return successResponse(examResponseDTO);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Create a new exam")
    public ResponseEntity<ApiResponse<ExamResponseDTO>> createExam(@Valid @RequestBody ExamRequestDTO requestDTO) {
        try {
            Exam exam = mapToEntity(requestDTO, Exam.class);
            Exam createdExam = examService.createExam(exam);
            ExamResponseDTO responseDTO = mapToDTO(createdExam, ExamResponseDTO.class);
            return createdResponse(responseDTO);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Update an existing exam")
    public ResponseEntity<ApiResponse<ExamResponseDTO>> updateExam(
            @PathVariable Long id, 
            @Valid @RequestBody ExamRequestDTO requestDTO) {
        try {
            Exam exam = mapToEntity(requestDTO, Exam.class);
            Exam updatedExam = examService.updateExam(id, exam);
            ExamResponseDTO responseDTO = mapToDTO(updatedExam, ExamResponseDTO.class);
            return successResponse(responseDTO);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an exam (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable Long id) {
        try {
            examService.deleteExam(id);
            return noContentResponse();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Get exams by status")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getExamsByStatus(@PathVariable Exam.ExamStatus status) {
        List<Exam> exams = examService.findByStatus(status);
        List<ExamResponseDTO> examResponseDTOs = mapToDTO(exams, ExamResponseDTO.class);
        return successResponse(examResponseDTOs);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Publish an exam")
    public ResponseEntity<ApiResponse<ExamResponseDTO>> publishExam(@PathVariable Long id) {
        try {
            Exam publishedExam = examService.publishExam(id);
            ExamResponseDTO responseDTO = mapToDTO(publishedExam, ExamResponseDTO.class);
            return successResponse(responseDTO);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Archive an exam")
    public ResponseEntity<ApiResponse<ExamResponseDTO>> archiveExam(@PathVariable Long id) {
        try {
            Exam archivedExam = examService.archiveExam(id);
            ExamResponseDTO responseDTO = mapToDTO(archivedExam, ExamResponseDTO.class);
            return successResponse(responseDTO);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    

}
