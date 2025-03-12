package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.Question;

import java.util.List;
import java.util.Optional;

public interface QuestionService {
    /**
     * Get all questions for an exam
     */
    List<Question> getQuestionsByExamId(Long examId);
    
    /**
     * Find a question by ID
     */
    Optional<Question> getQuestionById(Long id);
    
    /**
     * Find a question by ID (aliased method)
     */
    Optional<Question> findById(Long id);
    
    /**
     * Find all questions for an exam
     */
    List<Question> findByExamId(Long examId);
    
    /**
     * Find questions by topic
     */
    List<Question> findByTopic(String topic);
    
    /**
     * Find questions by difficulty level
     */
    List<Question> findByDifficulty(String difficulty);
    
    /**
     * Find random questions with optional filters
     */
    List<Question> findRandom(int count, String topic, String difficulty);
    
    /**
     * Create a new question
     */
    Question createQuestion(Question question);
    
    /**
     * Update an existing question
     */
    Question updateQuestion(Long id, Question question);
    
    /**
     * Delete a question
     */
    void deleteQuestion(Long id);
    
    /**
     * Get the highest question number for an exam
     */
    Integer getMaxQuestionNumberByExamId(Long examId);
    
    /**
     * Count questions for an exam
     */
    Long countQuestionsByExamId(Long examId);
}