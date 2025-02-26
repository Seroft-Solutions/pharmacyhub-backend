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
}
