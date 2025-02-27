package com.pharmacyhub.controller;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.domain.entity.Option;
import com.pharmacyhub.domain.entity.Question;
import com.pharmacyhub.dto.ExamDTO;
import com.pharmacyhub.service.ExamService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/exams")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExamController {

    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<ExamDTO>> getAllExams() {
        List<Exam> exams = examService.findAllActive();
        List<ExamDTO> examDTOs = exams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(examDTOs);
    }

    /**
     * Get published exams - publicly accessible without authentication
     */
    @GetMapping("/published")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ExamDTO>> getAllPublishedExams() {
        logger.info("Fetching all published exams");
        try {
            List<Exam> publishedExams = examService.findAllPublished();
            List<ExamDTO> examDTOs = publishedExams.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("Successfully fetched {} published exams", examDTOs.size());
            return ResponseEntity.ok(examDTOs);
        } catch (Exception e) {
            logger.error("Error fetching published exams: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR') or @examAccessEvaluator.canAccessExam(authentication, #id)")
    public ResponseEntity<ExamDTO> getExamById(@PathVariable Long id) {
        return examService.findById(id)
                .map(exam -> ResponseEntity.ok(convertToDTO(exam)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ExamDTO> createExam(@Valid @RequestBody ExamDTO examDTO) {
        try {
            Exam exam = convertToEntity(examDTO);
            Exam createdExam = examService.createExam(exam);
            return new ResponseEntity<>(convertToDTO(createdExam), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ExamDTO> updateExam(@PathVariable Long id, @Valid @RequestBody ExamDTO examDTO) {
        try {
            Exam exam = convertToEntity(examDTO);
            Exam updatedExam = examService.updateExam(id, exam);
            return ResponseEntity.ok(convertToDTO(updatedExam));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        try {
            examService.deleteExam(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<ExamDTO>> getExamsByStatus(@PathVariable Exam.ExamStatus status) {
        List<Exam> exams = examService.findByStatus(status);
        List<ExamDTO> examDTOs = exams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(examDTOs);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ExamDTO> publishExam(@PathVariable Long id) {
        try {
            Exam publishedExam = examService.publishExam(id);
            return ResponseEntity.ok(convertToDTO(publishedExam));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ExamDTO> archiveExam(@PathVariable Long id) {
        try {
            Exam archivedExam = examService.archiveExam(id);
            return ResponseEntity.ok(convertToDTO(archivedExam));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    
    /**
     * Convert Exam entity to ExamDTO
     * This moves the conversion logic from the entity to the controller
     * to avoid recursion issues during serialization
     */
    private ExamDTO convertToDTO(Exam exam) {
        ExamDTO dto = new ExamDTO();
        dto.setId(exam.getId());
        dto.setTitle(exam.getTitle());
        dto.setDescription(exam.getDescription());
        dto.setDuration(exam.getDuration());
        dto.setTotalMarks(exam.getTotalMarks());
        dto.setPassingMarks(exam.getPassingMarks());
        dto.setStatus(exam.getStatus());
        
        // Convert questions
        if (exam.getQuestions() != null) {
            exam.getQuestions().forEach(question -> {
                ExamDTO.QuestionDTO questionDTO = new ExamDTO.QuestionDTO();
                questionDTO.setId(question.getId());
                questionDTO.setQuestionNumber(question.getQuestionNumber());
                questionDTO.setQuestionText(question.getQuestionText());
                questionDTO.setCorrectAnswer(question.getCorrectAnswer());
                questionDTO.setExplanation(question.getExplanation());
                questionDTO.setMarks(question.getMarks());
                
                // Convert options
                if (question.getOptions() != null) {
                    question.getOptions().forEach(option -> {
                        ExamDTO.OptionDTO optionDTO = new ExamDTO.OptionDTO();
                        optionDTO.setId(option.getId());
                        optionDTO.setOptionKey(option.getLabel());
                        optionDTO.setOptionText(option.getText());
                        optionDTO.setIsCorrect(option.getIsCorrect());
                        questionDTO.getOptions().add(optionDTO);
                    });
                }
                
                dto.getQuestions().add(questionDTO);
            });
        }
        
        return dto;
    }
    
    /**
     * Convert ExamDTO to Exam entity
     */
    private Exam convertToEntity(ExamDTO dto) {
        Exam exam = new Exam();
        exam.setId(dto.getId());
        exam.setTitle(dto.getTitle());
        exam.setDescription(dto.getDescription());
        exam.setDuration(dto.getDuration());
        exam.setTotalMarks(dto.getTotalMarks());
        exam.setPassingMarks(dto.getPassingMarks());
        exam.setStatus(dto.getStatus());
        
        // Handle questions if they exist
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            for (ExamDTO.QuestionDTO questionDTO : dto.getQuestions()) {
                Question question = new Question();
                question.setQuestionNumber(questionDTO.getQuestionNumber());
                question.setQuestionText(questionDTO.getQuestionText());
                question.setCorrectAnswer(questionDTO.getCorrectAnswer());
                question.setExplanation(questionDTO.getExplanation());
                question.setMarks(questionDTO.getMarks());
                
                // Link question to exam
                question.setExam(exam);
                exam.addQuestion(question);
                
                // Handle options
                if (questionDTO.getOptions() != null && !questionDTO.getOptions().isEmpty()) {
                    for (ExamDTO.OptionDTO optionDTO : questionDTO.getOptions()) {
                        Option option = new Option();
                        option.setLabel(optionDTO.getOptionKey());
                        option.setText(optionDTO.getOptionText());
                        option.setIsCorrect(optionDTO.getIsCorrect());
                        
                        // Link option to question
                        option.setQuestion(question);
                        question.addOption(option);
                    }
                }
            }
        }
        
        return exam;
    }
}
