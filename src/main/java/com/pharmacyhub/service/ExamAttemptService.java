package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.FlaggedQuestion;
import com.pharmacyhub.domain.entity.ExamAttempt;
import com.pharmacyhub.domain.entity.ExamResult;
import com.pharmacyhub.domain.entity.UserAnswer;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptService {
    
    List<ExamAttempt> getAttemptsByUserId(String userId);
    
    List<ExamAttempt> getAttemptsByExamIdAndUserId(Long examId, String userId);
    
    Optional<ExamAttempt> getAttemptById(Long id);
    
    ExamAttempt startExam(Long examId, String userId);
    
    ExamAttempt saveUserAnswer(Long attemptId, UserAnswer userAnswer);
    
    ExamResult submitExam(Long attemptId, List<UserAnswer> userAnswers);
    
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
     * @return a list of flagged questions
     */
    List<FlaggedQuestion> getFlaggedQuestions(Long attemptId);
}
