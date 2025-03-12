package com.pharmacyhub.deprecated.repository;

// DEPRECATED: This repository is for the deprecated ExamPaper entity
// Use ExamRepository for all new development

import com.pharmacyhub.deprecated.entity.ExamPaper;
import com.pharmacyhub.deprecated.entity.ExamPaper.PaperType;
import com.pharmacyhub.deprecated.entity.ExamPaper.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing ExamPaper entities
 */
@Repository
public interface ExamPaperRepository extends JpaRepository<ExamPaper, Long> {
    
    /**
     * Find all non-deleted exam papers
     */
    List<ExamPaper> findByDeletedFalse();
    
    /**
     * Find all active (non-deleted) exam papers
     */
    @Query("SELECT p FROM ExamPaper p WHERE p.deleted = false")
    List<ExamPaper> findAllActive();
    
    /**
     * Find all non-deleted model papers
     */
    List<ExamPaper> findByTypeAndDeletedFalse(PaperType type);
    
    /**
     * Find all non-deleted model papers
     */
    @Query("SELECT p FROM ExamPaper p WHERE p.type = 'MODEL' AND p.deleted = false")
    List<ExamPaper> findAllModelPapers();
    
    /**
     * Find all non-deleted past papers
     */
    @Query("SELECT p FROM ExamPaper p WHERE p.type = 'PAST' AND p.deleted = false")
    List<ExamPaper> findAllPastPapers();
    
    /**
     * Find a non-deleted paper by ID
     */
    @Query("SELECT p FROM ExamPaper p WHERE p.id = ?1 AND p.deleted = false")
    Optional<ExamPaper> findByIdAndNotDeleted(Long id);
    
    /**
     * Find all non-deleted papers of a specific difficulty
     */
    List<ExamPaper> findByDifficultyAndDeletedFalse(Difficulty difficulty);
    
    /**
     * Find all non-deleted papers matching the specified tag
     */
    @Query("SELECT p FROM ExamPaper p JOIN p.tags t WHERE t = ?1 AND p.deleted = false")
    List<ExamPaper> findByTagAndDeletedFalse(String tag);
    
    /**
     * Find all non-deleted papers by exam ID
     */
    List<ExamPaper> findByExamIdAndDeletedFalse(Long examId);
    
    /**
     * Find all papers and calculate usage statistics
     */
    @Query("SELECT p FROM ExamPaper p WHERE p.deleted = false ORDER BY p.attemptCount DESC")
    List<ExamPaper> findTopAttemptedPapers();
    
    /**
     * Count total number of papers by type
     */
    long countByTypeAndDeletedFalse(PaperType type);
    
    /**
     * Calculate average duration across all papers
     */
    @Query("SELECT AVG(p.durationMinutes) FROM ExamPaper p WHERE p.deleted = false")
    Double getAverageDuration();
    
    /**
     * Count total papers in the system
     */
    @Query("SELECT COUNT(p) FROM ExamPaper p WHERE p.deleted = false")
    Long getTotalPaperCount();
    
    /**
     * Check if a paper with the given title exists
     */
    @Query("SELECT COUNT(p) > 0 FROM ExamPaper p WHERE p.title = ?1 AND p.deleted = false")
    boolean existsByTitle(String title);
}