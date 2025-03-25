package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.*;
import com.pharmacyhub.domain.repository.ExamAttemptRepository;
import com.pharmacyhub.domain.repository.ExamRepository;
import com.pharmacyhub.domain.repository.ExamResultRepository;
import com.pharmacyhub.domain.repository.UserAnswerRepository;
import com.pharmacyhub.domain.repository.FlaggedQuestionRepository;
import com.pharmacyhub.domain.repository.QuestionRepository;
import com.pharmacyhub.dto.ExamResultDTO;
import com.pharmacyhub.dto.response.ExamAttemptResponseDTO;
import com.pharmacyhub.dto.response.FlaggedQuestionResponseDTO;
import com.pharmacyhub.dto.request.AnswerSubmissionDTO;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExamAttemptServiceImpl implements ExamAttemptService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExamAttemptServiceImpl.class);
    
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamRepository examRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ExamResultRepository examResultRepository;
    private final FlaggedQuestionRepository flaggedQuestionRepository;
    private final QuestionRepository questionRepository;
    
    public ExamAttemptServiceImpl(
            ExamAttemptRepository examAttemptRepository,
            ExamRepository examRepository,
            UserAnswerRepository userAnswerRepository,
            ExamResultRepository examResultRepository,
            FlaggedQuestionRepository flaggedQuestionRepository,
            QuestionRepository questionRepository) {
        this.examAttemptRepository = examAttemptRepository;
        this.examRepository = examRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.examResultRepository = examResultRepository;
        this.flaggedQuestionRepository = flaggedQuestionRepository;
        this.questionRepository = questionRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamAttemptResponseDTO> getAttemptsByUserId(String userId) {
        List<ExamAttempt> attempts = examAttemptRepository.findByUserId(userId);
        return attempts.stream()
                .map(this::mapToExamAttemptResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamAttemptResponseDTO> getAttemptsByExamAndUserId(Long examId, String userId) {
        List<ExamAttempt> attempts = examAttemptRepository.findByExamIdAndUserId(examId, userId);
        return attempts.stream()
                .map(this::mapToExamAttemptResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExamAttemptResponseDTO getAttemptById(Long id) {
        ExamAttempt attempt = examAttemptRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam attempt not found with id: " + id));
        return mapToExamAttemptResponseDTO(attempt);
    }
    
    @Override
    public ExamAttemptResponseDTO startExam(Long examId, String userId) {
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
        
        ExamAttempt savedAttempt = examAttemptRepository.save(attempt);
        return mapToExamAttemptResponseDTO(savedAttempt);
    }
    
    @Override
    public void saveAnswer(Long attemptId, Long questionId, String selectedOptionId, Integer timeSpent) {
        ExamAttempt attempt = examAttemptRepository.findByIdAndNotDeleted(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Exam attempt not found with id: " + attemptId));
        
        if (attempt.getStatus() != ExamAttempt.AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot save answer for an exam that is not in progress");
        }
        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));
        
        // Check if an answer for this question already exists
        Optional<UserAnswer> existingAnswer = userAnswerRepository.findByAttemptIdAndQuestionId(
                attemptId, questionId);
        
        if (existingAnswer.isPresent()) {
            // Update existing answer
            UserAnswer answer = existingAnswer.get();
            answer.setSelectedOptionId(selectedOptionId);
            answer.setTimeSpent(timeSpent);
            userAnswerRepository.save(answer);
        } else {
            // Save new answer
            UserAnswer newAnswer = new UserAnswer();
            newAnswer.setAttempt(attempt);
            newAnswer.setQuestion(question);
            newAnswer.setSelectedOptionId(selectedOptionId);
            newAnswer.setTimeSpent(timeSpent);
            userAnswerRepository.save(newAnswer);
            attempt.getAnswers().add(newAnswer);
        }
        
        examAttemptRepository.save(attempt);
    }
    
    @Override
    public ExamResultDTO submitExamAttempt(Long attemptId) {
        ExamAttempt attempt = examAttemptRepository.findByIdAndNotDeleted(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Exam attempt not found with id: " + attemptId));
        
        if (attempt.getStatus() != ExamAttempt.AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot submit an exam that is not in progress");
        }
        
        // Update attempt status
        attempt.setStatus(ExamAttempt.AttemptStatus.COMPLETED);
        attempt.setEndTime(LocalDateTime.now());
        examAttemptRepository.save(attempt);
        
        // Calculate and save result
        ExamResult result = calculateExamResult(attempt);
        
        // Map to DTO
        return mapToExamResultDTO(result, attempt);
    }
    
    @Override
    @Transactional
    public ExamResultDTO submitExamWithAnswers(Long attemptId, List<AnswerSubmissionDTO> finalAnswers) {
        logger.info("Submitting exam with answers for attempt ID: {}", attemptId);
        
        ExamAttempt attempt = examAttemptRepository.findByIdAndNotDeleted(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Exam attempt not found with id: " + attemptId));
        
        if (attempt.getStatus() != ExamAttempt.AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot submit an exam that is not in progress");
        }
        
        // Process any final answers if provided
        if (finalAnswers != null && !finalAnswers.isEmpty()) {
            logger.info("Processing {} final answers for attempt ID: {}", finalAnswers.size(), attemptId);
            
            for (AnswerSubmissionDTO answer : finalAnswers) {
                Question question = questionRepository.findById(answer.getQuestionId())
                        .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + answer.getQuestionId()));
                
                // Check if an answer for this question already exists
                Optional<UserAnswer> existingAnswer = userAnswerRepository.findByAttemptIdAndQuestionId(
                        attemptId, answer.getQuestionId());
                
                if (existingAnswer.isPresent()) {
                    // Update existing answer
                    UserAnswer userAnswer = existingAnswer.get();
                    userAnswer.setSelectedOptionId(answer.getSelectedOptionId());
                    userAnswer.setTimeSpent(answer.getTimeSpent());
                    userAnswerRepository.save(userAnswer);
                } else {
                    // Save new answer
                    UserAnswer newAnswer = new UserAnswer();
                    newAnswer.setAttempt(attempt);
                    newAnswer.setQuestion(question);
                    newAnswer.setSelectedOptionId(answer.getSelectedOptionId());
                    newAnswer.setTimeSpent(answer.getTimeSpent());
                    userAnswerRepository.save(newAnswer);
                    attempt.getAnswers().add(newAnswer);
                }
            }
        }
        
        // Update attempt status
        attempt.setStatus(ExamAttempt.AttemptStatus.COMPLETED);
        attempt.setEndTime(LocalDateTime.now());
        examAttemptRepository.save(attempt);
        
        // Calculate and save result
        ExamResult result = calculateExamResult(attempt);
        
        logger.info("Successfully submitted exam for attempt ID: {}", attemptId);
        
        // Map to DTO
        return mapToExamResultDTO(result, attempt);
    }
    
    @Override
    public ExamResultDTO getExamResult(Long attemptId) {
        ExamAttempt attempt = examAttemptRepository.findByIdAndNotDeleted(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Exam attempt not found with id: " + attemptId));
        
        if (attempt.getStatus() != ExamAttempt.AttemptStatus.COMPLETED) {
            throw new IllegalStateException("Cannot get result for an exam that is not completed");
        }
        
        ExamResult result = examResultRepository.findByAttemptId(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Exam result not found for attempt id: " + attemptId));
        
        return mapToExamResultDTO(result, attempt);
    }
    
    @Override
    public ExamAttempt flagQuestion(Long attemptId, Long questionId) {
        ExamAttempt attempt = examAttemptRepository.findByIdAndNotDeleted(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Exam attempt not found with id: " + attemptId));
        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));
        
        // Check if question belongs to the exam
        if (!question.getExam().getId().equals(attempt.getExam().getId())) {
            throw new IllegalArgumentException("Question does not belong to this exam");
        }
        
        // Check if question is already flagged
        if (flaggedQuestionRepository.existsByAttemptIdAndQuestionId(attemptId, questionId)) {
            // Already flagged, nothing to do
            return attempt;
        }
        
        // Create new flagged question
        FlaggedQuestion flaggedQuestion = new FlaggedQuestion();
        flaggedQuestion.setAttempt(attempt);
        flaggedQuestion.setQuestion(question);
        
        flaggedQuestionRepository.save(flaggedQuestion);
        
        return attempt;
    }
    
    @Override
    public ExamAttempt unflagQuestion(Long attemptId, Long questionId) {
        ExamAttempt attempt = examAttemptRepository.findByIdAndNotDeleted(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Exam attempt not found with id: " + attemptId));
        
        flaggedQuestionRepository.findByAttemptIdAndQuestionIdAndDeletedFalse(attemptId, questionId)
                .ifPresent(flaggedQuestion -> {
                    flaggedQuestion.setDeleted(true);
                    flaggedQuestionRepository.save(flaggedQuestion);
                });
        
        return attempt;
    }
    
    @Override
    public List<FlaggedQuestionResponseDTO> getFlaggedQuestions(Long attemptId) {
        // Check if attempt exists
        if (!examAttemptRepository.existsById(attemptId)) {
            throw new EntityNotFoundException("Exam attempt not found with id: " + attemptId);
        }
        
        List<FlaggedQuestion> flaggedQuestions = flaggedQuestionRepository.findByAttemptIdAndDeletedFalse(attemptId);
        
        return flaggedQuestions.stream()
                .map(this::mapToFlaggedQuestionResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate exam result based on user answers
     */
    private ExamResult calculateExamResult(ExamAttempt attempt) {
        Exam exam = attempt.getExam();
        List<Question> questions = questionRepository.findByExamId(exam.getId());
        List<UserAnswer> userAnswers = userAnswerRepository.findByAttemptId(attempt.getId());
        
        int totalQuestions = questions.size();
        int correctCount = 0;
        int incorrectCount = 0; // Added declaration and initialization of incorrectCount
        int totalTimeSpent = 0;
        
        // Map of questionId to Question for quick lookup
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        
        // Map of questionId to UserAnswer for quick lookup
        Map<Long, UserAnswer> answerMap = userAnswers.stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getId(),
                        answer -> answer,
                        (a1, a2) -> a1 // Keep first in case of duplicate (should not happen)
                ));
        
        // Calculate scores
        List<ExamResultDTO.QuestionResultDTO> questionResults = new ArrayList<>();
        
        for (Question question : questions) {
            UserAnswer userAnswer = answerMap.get(question.getId());
            boolean isCorrect = false;
            String userAnswerId = null;
            int timeSpent = 0;
            
            if (userAnswer != null) {
                userAnswerId = userAnswer.getSelectedOptionId();
                timeSpent = userAnswer.getTimeSpent();
                totalTimeSpent += timeSpent;
                
                // Check if answer is correct
                isCorrect = userAnswerId != null && userAnswerId.equals(question.getCorrectAnswer());
                if (isCorrect) {
                    correctCount++;
                } else if (userAnswerId != null) {
                    // Count as incorrect answer only if option was selected (not empty)
                    incorrectCount++;
                }
            }
        }
        
        // Calculate unanswered questions
        int unansweredCount = totalQuestions - correctCount - incorrectCount;
        
        // Calculate score with negative marking
        // Each correct answer: +1 mark
        // Each incorrect answer: -0.25 mark
        // Unanswered questions: 0 marks
        double rawScore = correctCount * 1.0 - incorrectCount * 0.25;
        
        // Convert to percentage
        double score = (rawScore / totalQuestions) * 100;
        
        // Check if passed - minimum passing mark is 40
        boolean isPassed = score >= Math.max(40, exam.getPassingMarks());
        
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
    
    /**
     * Map ExamAttempt entity to ExamAttemptResponseDTO
     */
    private ExamAttemptResponseDTO mapToExamAttemptResponseDTO(ExamAttempt attempt) {
        ExamAttemptResponseDTO dto = new ExamAttemptResponseDTO();
        dto.setId(attempt.getId());
        dto.setExamId(attempt.getExam().getId());
        dto.setExamTitle(attempt.getExam().getTitle());
        dto.setUserId(attempt.getUserId());
        dto.setStartTime(attempt.getStartTime().toString());
        if (attempt.getEndTime() != null) {
            dto.setEndTime(attempt.getEndTime().toString());
        }
        dto.setStatus(attempt.getStatus().toString());
        
        return dto;
    }
    
    /**
     * Map FlaggedQuestion entity to FlaggedQuestionResponseDTO
     */
    private FlaggedQuestionResponseDTO mapToFlaggedQuestionResponseDTO(FlaggedQuestion flaggedQuestion) {
        FlaggedQuestionResponseDTO dto = new FlaggedQuestionResponseDTO();
        dto.setQuestionId(flaggedQuestion.getQuestion().getId());
        dto.setAttemptId(flaggedQuestion.getAttempt().getId());
        dto.setQuestionText(flaggedQuestion.getQuestion().getQuestionText());
        return dto;
    }
    
    /**
     * Map ExamResult entity to ExamResultDTO
     */
    private ExamResultDTO mapToExamResultDTO(ExamResult result, ExamAttempt attempt) {
        Exam exam = attempt.getExam();
        List<UserAnswer> userAnswers = userAnswerRepository.findByAttemptId(attempt.getId());
        List<Question> questions = questionRepository.findByExamId(exam.getId());
        
        // Map of questionId to Question for quick lookup
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        
        // Map of questionId to UserAnswer for quick lookup
        Map<Long, UserAnswer> answerMap = userAnswers.stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getId(),
                        answer -> answer,
                        (a1, a2) -> a1 // Keep first in case of duplicate (should not happen)
                ));
        
        // Create question results
        List<ExamResultDTO.QuestionResultDTO> questionResults = new ArrayList<>();
        
        for (Question question : questions) {
            UserAnswer userAnswer = answerMap.get(question.getId());
            boolean isCorrect = false;
            String userAnswerId = null;
            int earnedPoints = 0;
            
            if (userAnswer != null) {
                userAnswerId = userAnswer.getSelectedOptionId();
                
                // Check if answer is correct
                isCorrect = userAnswerId != null && userAnswerId.equals(question.getCorrectAnswer());
                if (isCorrect) {
                    earnedPoints = question.getMarks();
                }
            }
            
            ExamResultDTO.QuestionResultDTO questionResult = ExamResultDTO.QuestionResultDTO.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .userAnswerId(userAnswerId)
                    .correctAnswerId(question.getCorrectAnswer())
                    .isCorrect(isCorrect)
                    .explanation(question.getExplanation())
                    .points(question.getMarks())
                    .earnedPoints(earnedPoints)
                    .build();
            
            questionResults.add(questionResult);
        }
        
        // Build the result DTO
        return ExamResultDTO.builder()
                .examId(exam.getId())
                .examTitle(exam.getTitle())
                .score(result.getScore())
                .totalMarks(exam.getTotalMarks())
                .passingMarks(exam.getPassingMarks())
                .isPassed(result.getIsPassed())
                .timeSpent(result.getTimeSpent())
                .questionResults(questionResults)
                .build();
    }
}