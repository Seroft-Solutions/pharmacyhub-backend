package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.FlaggedQuestion;
import com.pharmacyhub.domain.entity.ExamAttempt;
import com.pharmacyhub.domain.entity.ExamResult;
import com.pharmacyhub.domain.entity.UserAnswer;
import com.pharmacyhub.dto.ExamResultDTO;
import com.pharmacyhub.dto.response.ExamAttemptResponseDTO;
import com.pharmacyhub.dto.response.FlaggedQuestionResponseDTO;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptService {
    
    List<ExamAttemptResponseDTO> getAttemptsByUserId(String userId);
    
    List<ExamAttemptResponseDTO> getAttemptsByExamAndUserId(Long examId, String userId);
    
    ExamAttemptResponseDTO getAttemptById(Long id);
    
    ExamAttemptResponseDTO startExam(Long examId, String userId);
    
    void saveAnswer(Long attemptId, Long questionId, String selectedOptionId, Integer timeSpent);
    
    /**
     * Submit an exam and calculate the result
     * @param attemptId the exam attempt ID
     * @return the calculated exam result
     */
    ExamResultDTO submitExamAttempt(Long attemptId);
    
    /**
     * Get the result for a completed exam
     * @param attemptId the exam attempt ID
     * @return the exam result
     */
    ExamResultDTO getExamResult(Long attemptId);
    
    /**
     * Flag a question for review later
     * @param attemptId the exam attempt ID
     * @param questionId the question ID to flag
     * @return the updated exam attempt
     */
    ExamAttempt flagQuestion(Long attemptId, Long questionId);
    
    /**
     * Unflag a previously flagged question
     * @param attemptId the exam attempt ID
     * @param questionId the question ID to unflag
     * @return the updated exam attempt
     */
    ExamAttempt unflagQuestion(Long attemptId, Long questionId);
    
    /**
     * Get all flagged questions for an attempt
     * @param attemptId the exam attempt ID
     * @return a list of flagged questions DTOs
     */
    List<FlaggedQuestionResponseDTO> getFlaggedQuestions(Long attemptId);
}