package com.pharmacyhub.domain.repository;

import com.pharmacyhub.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.exam.id = :examId AND q.deleted = false ORDER BY q.questionNumber")
    List<Question> findByExamId(Long examId);
    
    /**
     * Find all questions for an exam that are not deleted
     */
    @Query("SELECT q FROM Question q WHERE q.exam.id = :examId AND q.deleted = false ORDER BY q.questionNumber")
    List<Question> findByExamIdAndDeletedFalse(Long examId);

    @Query("SELECT q FROM Question q WHERE q.deleted = false AND q.id = :id")
    Optional<Question> findByIdAndNotDeleted(Long id);
    
    /**
     * Find question by ID and not deleted
     */
    @Query("SELECT q FROM Question q WHERE q.deleted = false AND q.id = :id")
    Optional<Question> findById(Long id);

    @Query("SELECT COALESCE(MAX(q.questionNumber), 0) FROM Question q WHERE q.exam.id = :examId")
    Integer findMaxQuestionNumberByExamId(Long examId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.exam.id = :examId AND q.deleted = false")
    Long countByExamId(Long examId);
    
    /**
     * Count questions for an exam that are not deleted
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.exam.id = :examId AND q.deleted = false")
    Integer countByExamIdAndDeletedFalse(Long examId);
    
    /**
     * Find questions by topic
     */
    @Query("SELECT q FROM Question q WHERE q.deleted = false AND LOWER(q.topic) = LOWER(:topic) ORDER BY q.id")
    List<Question> findByTopic(@Param("topic") String topic);
    
    /**
     * Find questions by difficulty level
     */
    @Query("SELECT q FROM Question q WHERE q.deleted = false AND LOWER(q.difficulty) = LOWER(:difficulty) ORDER BY q.id")
    List<Question> findByDifficulty(@Param("difficulty") String difficulty);
    
    /**
     * Find random questions with optional filters
     */
    @Query("SELECT q FROM Question q WHERE q.deleted = false " +
           "AND (:topic IS NULL OR LOWER(q.topic) = LOWER(:topic)) " +
           "AND (:difficulty IS NULL OR LOWER(q.difficulty) = LOWER(:difficulty)) " +
           "ORDER BY RAND() LIMIT :count")
    List<Question> findRandom(@Param("count") int count, 
                             @Param("topic") String topic, 
                             @Param("difficulty") String difficulty);
}
