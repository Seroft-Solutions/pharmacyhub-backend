package com.pharmacyhub.domain.repository;

import com.pharmacyhub.domain.entity.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing ExamAttempt entities
 */
@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    
    /**
     * Find all non-deleted attempts by user ID
     */
    @Query("SELECT a FROM ExamAttempt a WHERE a.userId = ?1 AND a.deleted = false ORDER BY a.startTime DESC")
    List<ExamAttempt> findByUserId(String userId);
    
    /**
     * Find all non-deleted attempts by exam ID and user ID
     */
    @Query("SELECT a FROM ExamAttempt a WHERE a.exam.id = ?1 AND a.userId = ?2 AND a.deleted = false ORDER BY a.startTime DESC")
    List<ExamAttempt> findByExamIdAndUserId(Long examId, String userId);
    
    /**
     * Find a specific non-deleted attempt by ID
     */
    @Query("SELECT a FROM ExamAttempt a WHERE a.id = ?1 AND a.deleted = false")
    Optional<ExamAttempt> findByIdAndNotDeleted(Long id);
    
    /**
     * Find the most recent in-progress attempt by user ID and exam ID
     */
    @Query("SELECT a FROM ExamAttempt a WHERE a.exam.id = ?1 AND a.userId = ?2 AND a.status = ?3 AND a.deleted = false " +
           "ORDER BY a.startTime DESC")
    Optional<ExamAttempt> findFirstByExamIdAndUserIdAndStatusAndDeletedFalse(
            Long examId, String userId, ExamAttempt.AttemptStatus status);
    
    /**
     * Count all completed attempts for a specific exam
     */
    @Query("SELECT COUNT(a) FROM ExamAttempt a WHERE a.exam.id = ?1 AND a.status = ?2 AND a.deleted = false")
    Long countByExamIdAndStatusAndDeletedFalse(Long examId, ExamAttempt.AttemptStatus status);
    
    /**
     * Calculate completion rate for a specific exam (percentage of completed attempts)
     */
    @Query("SELECT COUNT(a) FROM ExamAttempt a WHERE a.exam.id = ?1 AND a.status = 'COMPLETED' AND a.deleted = false")
    long countCompletedAttemptsByExamId(Long examId);
    
    /**
     * Calculate average score for a specific exam
     */
    @Query("SELECT AVG(r.score) FROM ExamResult r JOIN r.attempt a WHERE a.exam.id = ?1 AND a.deleted = false")
    Double getAverageScoreByExamId(Long examId);
    
    /**
     * Count distinct user IDs with at least one attempt
     */
    @Query("SELECT COUNT(DISTINCT a.userId) FROM ExamAttempt a WHERE a.deleted = false")
    long countDistinctUserIds();
    
    /**
     * Count unique users who have made attempts since a specific date
     */
    @Query("SELECT COUNT(DISTINCT a.userId) FROM ExamAttempt a WHERE a.startTime >= :since AND a.deleted = false")
    long countUniqueUsersSince(@Param("since") LocalDateTime since);
}