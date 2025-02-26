package com.pharmacyhub.domain.repository;

import com.pharmacyhub.domain.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    @Query("SELECT a FROM UserAnswer a WHERE a.deleted = false AND a.attempt.id = :attemptId")
    List<UserAnswer> findByAttemptId(Long attemptId);
    
    @Query("SELECT a FROM UserAnswer a WHERE a.deleted = false AND a.attempt.id = :attemptId AND a.question.id = :questionId")
    Optional<UserAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);
}
