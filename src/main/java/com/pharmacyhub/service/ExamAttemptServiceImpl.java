package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.*;
import com.pharmacyhub.domain.repository.ExamAttemptRepository;
import com.pharmacyhub.domain.repository.ExamRepository;
import com.pharmacyhub.domain.repository.ExamResultRepository;
import com.pharmacyhub.domain.repository.UserAnswerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExamAttemptServiceImpl implements ExamAttemptService {
    
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamRepository examRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ExamResultRepository examResultRepository;
    
    public ExamAttemptServiceImpl(
            ExamAttemptRepository examAttemptRepository,
            ExamRepository examRepository,
            UserAnswerRepository userAnswerRepository,
            ExamResultRepository examResultRepository) {
        this.examAttemptRepository = examAttemptRepository;
        this.examRepository = examRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.examResultRepository = examResultRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamAttempt> getAttemptsByUserId(String userId) {
        return examAttemptRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamAttempt> getAttemptsByExamIdAndUserId(Long examId, String userId) {
        return examAttemptRepository.findByExamIdAndUserId(examId, userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ExamAttempt> getAttemptById(Long id) {
        return examAttemptRepository.findByIdAndNotDeleted(id);
    }
    
    @Override
    public ExamAttempt startExam(Long examId, String userId) {
        Exam exam = examRepository.findByIdAndNotDeleted(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with id: " + examId));
        
        if (exam.getStatus() != Exam.ExamStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot start an exam that is not published");
        }
        
        ExamAttempt attempt = new ExamAttempt();
        attempt.setExam(exam);
        attempt.setUserId(userId);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setStatus(ExamAttempt.AttemptStatus.IN_PROGRESS);
        
        return examAttemptRepository.save(attempt);
    }
    
    @Override
    public ExamAttempt saveUserAnswer(Long attemptId, UserAnswer userAnswer) {
        ExamAttempt attempt = examAttemptRepository.findByIdAndNotDeleted(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Exam attempt not found with id: " + attemptId));
        
        if (attempt.getStatus() != ExamAttempt.AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot save answer for an exam that is not in progress");
        }
        
        // Check if an answer for this question already exists
        Optional<UserAnswer> existingAnswer = userAnswerRepository.findByAttemptIdAndQuestionId(
                attemptId, userAnswer.getQuestion().getId());
        
        if (existingAnswer.isPresent()) {
            // Update existing answer
            UserAnswer answer = existingAnswer.get();
            answer.setSelectedOptionId(userAnswer.getSelectedOptionId());
            answer.setTimeSpent(userAnswer.getTimeSpent());
            userAnswerRepository.save(answer);
        } else {
            // Save new answer
            userAnswer.setAttempt(attempt);
            attempt.addAnswer(userAnswer);
        }
        
        return examAttemptRepository.save(attempt);
    }
    
    @Override
    public ExamResult submitExam(Long attemptId, List<UserAnswer> userAnswers) {
        ExamAttempt attempt = examAttemptRepository.findByIdAndNotDeleted(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Exam attempt not found with id: " + attemptId));
        
        if (attempt.getStatus() != ExamAttempt.AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot submit an exam that is not in progress");
        }
        
        Exam exam = attempt.getExam();
        
        // Save each user answer
        for (UserAnswer answer : userAnswers) {
            answer.setAttempt(attempt);
            userAnswerRepository.save(answer);
        }
        
        // Update attempt status
        attempt.setStatus(ExamAttempt.AttemptStatus.COMPLETED);
        attempt.setEndTime(LocalDateTime.now());
        examAttemptRepository.save(attempt);
        
        // Calculate results
        List<Question> questions = exam.getQuestions();
        int totalQuestions = questions.size();
        int totalAnswered = userAnswers.size();
        int correctCount = 0;
        int totalTimeSpent = 0;
        
        for (UserAnswer answer : userAnswers) {
            Question question = questions.stream()
                    .filter(q -> q.getId().equals(answer.getQuestion().getId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Question not found in exam"));
            
            totalTimeSpent += answer.getTimeSpent();
            
            if (question.getCorrectAnswer().equals(answer.getSelectedOptionId())) {
                correctCount++;
            }
        }
        
        int incorrectCount = totalAnswered - correctCount;
        int unansweredCount = totalQuestions - totalAnswered;
        
        // Calculate score as a percentage
        double score = (double) correctCount / totalQuestions * 100;
        boolean isPassed = score >= exam.getPassingMarks();
        
        // Create and save result
        ExamResult result = new ExamResult();
        result.setAttempt(attempt);
        result.setScore(score);
        result.setTotalQuestions(totalQuestions);
        result.setCorrectAnswers(correctCount);
        result.setIncorrectAnswers(incorrectCount);
        result.setUnanswered(unansweredCount);
        result.setTimeSpent(totalTimeSpent);
        result.setIsPassed(isPassed);
        result.setCompletedAt(LocalDateTime.now());
        
        return examResultRepository.save(result);
    }
}
