package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.dto.request.ExamFilterRequestDTO;
import com.pharmacyhub.dto.response.ExamResponseDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing exams
 */
public interface IExamService {
    
    /**
     * Find all active (non-deleted) exams
     */
    List<Exam> findAllActive();
    
    /**
     * Find all published exams
     */
    List<Exam> findAllPublished();
    
    /**
     * Find an exam by ID
     */
    Optional<Exam> findById(Long id);
    
    /**
     * Find exams by status
     */
    List<Exam> findByStatus(Exam.ExamStatus status);
    
    /**
     * Check if an exam with the given title exists
     */
    boolean existsByTitle(String title);
    
    /**
     * Find papers by filter criteria
     */
    List<ExamResponseDTO> findPapersByFilter(ExamFilterRequestDTO filters);
    
    /**
     * Find a paper by ID
     */
    ExamResponseDTO findPaperById(Long id);
    
    /**
     * Get exam statistics
     */
    Map<String, Object> getExamStats();
    
    /**
     * Create a new exam
     */
    Exam createExam(Exam exam);
    
    /**
     * Update an existing exam
     */
    Exam updateExam(Long id, Exam exam);
    
    /**
     * Delete an exam
     */
    void deleteExam(Long id);
    
    /**
     * Publish an exam
     */
    Exam publishExam(Long id);
    
    /**
     * Archive an exam
     */
    Exam archiveExam(Long id);
}
