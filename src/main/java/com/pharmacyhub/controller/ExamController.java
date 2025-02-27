package com.pharmacyhub.controller;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.dto.ExamDTO;
import com.pharmacyhub.service.ExamService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
    public ResponseEntity<List<Exam>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    /**
     * Get published exams - publicly accessible without authentication
     */
    @GetMapping("/published")
    public ResponseEntity<List<Exam>> getAllPublishedExams() {
        logger.info("Fetching all published exams");
        try {
            List<Exam> publishedExams = examService.getAllPublishedExams();
            logger.info("Successfully fetched {} published exams", publishedExams.size());
            return ResponseEntity.ok(publishedExams);
        } catch (Exception e) {
            logger.error("Error fetching published exams: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exam> getExamById(@PathVariable Long id) {
        return examService.getExamById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));
    }

    @PostMapping
    public ResponseEntity<Exam> createExam(@Valid @RequestBody ExamDTO examDTO) {
        try {
            Exam exam = convertToEntity(examDTO);
            Exam createdExam = examService.createExam(exam);
            return new ResponseEntity<>(createdExam, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Exam> updateExam(@PathVariable Long id, @Valid @RequestBody ExamDTO examDTO) {
        try {
            Exam exam = convertToEntity(examDTO);
            Exam updatedExam = examService.updateExam(id, exam);
            return ResponseEntity.ok(updatedExam);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        try {
            examService.deleteExam(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Exam>> getExamsByStatus(@PathVariable Exam.ExamStatus status) {
        return ResponseEntity.ok(examService.getExamsByStatus(status));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Exam> publishExam(@PathVariable Long id) {
        try {
            Exam publishedExam = examService.publishExam(id);
            return ResponseEntity.ok(publishedExam);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<Exam> archiveExam(@PathVariable Long id) {
        try {
            Exam archivedExam = examService.archiveExam(id);
            return ResponseEntity.ok(archivedExam);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private Exam convertToEntity(ExamDTO examDTO) {
        Exam exam = new Exam();
        exam.setTitle(examDTO.getTitle());
        exam.setDescription(examDTO.getDescription());
        exam.setDuration(examDTO.getDuration());
        exam.setTotalMarks(examDTO.getTotalMarks());
        exam.setPassingMarks(examDTO.getPassingMarks());
        return exam;
    }
}
