package com.pharmacyhub.domain.repository;

import com.pharmacyhub.domain.entity.FlaggedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlaggedQuestionRepository extends JpaRepository<FlaggedQuestion, Long> {
    
    @Query("SELECT f FROM FlaggedQuestion f WHERE f.deleted = false AND f.attempt.id = :attemptId")
    List<FlaggedQuestion> findByAttemptId(Long attemptId);
    
    @Query("SELECT f FROM FlaggedQuestion f WHERE f.deleted = false AND f.attempt.id = :attemptId AND f.question.id = :questionId")
    Optional<FlaggedQuestion> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);
    
    @Query("SELECT COUNT(f) > 0 FROM FlaggedQuestion f WHERE f.deleted = false AND f.attempt.id = :attemptId AND f.question.id = :questionId")
    boolean existsByAttemptIdAndQuestionId(Long attemptId, Long questionId);
}
