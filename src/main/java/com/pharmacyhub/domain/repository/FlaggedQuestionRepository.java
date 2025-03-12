package com.pharmacyhub.domain.repository;

import com.pharmacyhub.domain.entity.FlaggedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing FlaggedQuestion entities
 */
@Repository
public interface FlaggedQuestionRepository extends JpaRepository<FlaggedQuestion, Long> {
    
    /**
     * Find all flagged questions for a specific attempt
     */
    List<FlaggedQuestion> findByAttemptIdAndDeletedFalse(Long attemptId);
    
    /**
     * Find a specific flagged question by attempt ID and question ID
     */
    Optional<FlaggedQuestion> findByAttemptIdAndQuestionIdAndDeletedFalse(Long attemptId, Long questionId);
    
    /**
     * Check if a question is flagged for a specific attempt
     */
    default boolean existsByAttemptIdAndQuestionId(Long attemptId, Long questionId) {
        return findByAttemptIdAndQuestionIdAndDeletedFalse(attemptId, questionId).isPresent();
    }
    
    /**
     * Get all question IDs flagged for an attempt
     */
    @Query("SELECT f.question.id FROM FlaggedQuestion f WHERE f.attempt.id = ?1 AND f.deleted = false")
    List<Long> findQuestionIdsByAttemptId(Long attemptId);
    
    /**
     * Delete a flagged question by attempt and question IDs
     */
    @Modifying
    @Transactional
    @Query("UPDATE FlaggedQuestion f SET f.deleted = true WHERE f.attempt.id = ?1 AND f.question.id = ?2")
    void deleteByAttemptIdAndQuestionId(Long attemptId, Long questionId);
}