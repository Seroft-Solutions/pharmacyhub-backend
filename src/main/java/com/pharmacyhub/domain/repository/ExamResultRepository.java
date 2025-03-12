package com.pharmacyhub.domain.repository;

import com.pharmacyhub.domain.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    
    @Query("SELECT r FROM ExamResult r WHERE r.deleted = false AND r.attempt.id = :attemptId")
    Optional<ExamResult> findByAttemptId(Long attemptId);
    
    @Query("SELECT AVG(r.correctAnswers * 1.0 / r.totalQuestions) FROM ExamResult r WHERE r.deleted = false")
    Double getAverageCompletionRate();
    
    /**
     * Get the success rate (percentage of passes) for a specific exam
     */
    @Query("SELECT (COUNT(r) * 1.0 / NULLIF((SELECT COUNT(r2) FROM ExamResult r2 " +
           "JOIN r2.attempt a2 WHERE a2.exam.id = :examId AND r2.deleted = false), 0)) * 100 " +
           "FROM ExamResult r JOIN r.attempt a " +
           "WHERE a.exam.id = :examId AND r.isPassed = true AND r.deleted = false")
    Double getSuccessRateByExamId(Long examId);
}
