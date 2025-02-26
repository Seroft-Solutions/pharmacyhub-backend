package com.pharmacyhub.domain.repository;

import com.pharmacyhub.domain.entity.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    
    @Query("SELECT a FROM ExamAttempt a WHERE a.deleted = false AND a.id = :id")
    Optional<ExamAttempt> findByIdAndNotDeleted(Long id);
    
    @Query("SELECT a FROM ExamAttempt a WHERE a.deleted = false AND a.userId = :userId")
    List<ExamAttempt> findByUserId(String userId);
    
    @Query("SELECT a FROM ExamAttempt a WHERE a.deleted = false AND a.exam.id = :examId AND a.userId = :userId")
    List<ExamAttempt> findByExamIdAndUserId(Long examId, String userId);
    
    @Query("SELECT COUNT(DISTINCT a.userId) FROM ExamAttempt a WHERE a.deleted = false AND a.startTime > :since")
    Integer countUniqueUsersSince(LocalDateTime since);
    
    @Query("SELECT COUNT(a) FROM ExamAttempt a WHERE a.deleted = false AND a.status = 'COMPLETED'") 
    Integer countCompleted();
    
    @Query("SELECT COUNT(a) FROM ExamAttempt a WHERE a.deleted = false") 
    Integer countTotal();
}
