package com.pharmacyhub.service;

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
}
