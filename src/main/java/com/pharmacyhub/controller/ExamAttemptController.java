package com.pharmacyhub.controller;

import com.pharmacyhub.domain.entity.ExamAttempt;
import com.pharmacyhub.domain.entity.ExamResult;
import com.pharmacyhub.domain.entity.FlaggedQuestion;
import com.pharmacyhub.domain.entity.Question;
import com.pharmacyhub.domain.entity.UserAnswer;
import com.pharmacyhub.dto.ExamAttemptDTO;
import com.pharmacyhub.dto.ExamResultDTO;
import com.pharmacyhub.dto.FlaggedQuestionDTO;
import com.pharmacyhub.dto.UserAnswerDTO;
import com.pharmacyhub.service.ExamAttemptService;
import com.pharmacyhub.service.ExamService;
import com.pharmacyhub.service.QuestionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/exams")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExamAttemptController {
    
    private final ExamAttemptService examAttemptService;
    private final ExamService examService;
    private final QuestionService questionService;
    
    public ExamAttemptController(
            ExamAttemptService examAttemptService,
            ExamService examService,
            QuestionService questionService) {
        this.examAttemptService = examAttemptService;
        this.examService = examService;
        this.questionService = questionService;
    }
    
    @PostMapping("/{id}/start")
    @PreAuthorize("isAuthenticated() and @examAccessEvaluator.canAccessExam(authentication, #id)")
    public ResponseEntity<ExamAttemptDTO> startExam(@PathVariable Long id) {
        try {
            // Get user ID from authentication context
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            ExamAttempt attempt = examAttemptService.startExam(id, userId);
            return new ResponseEntity<>(convertToDTO(attempt), HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    
    @PostMapping("/attempts/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExamResultDTO> submitExam(
            @PathVariable Long id,
            @Valid @RequestBody List<UserAnswerDTO> userAnswerDTOs) {
        try {
            List<UserAnswer> userAnswers = userAnswerDTOs.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());
            
            ExamResult result = examAttemptService.submitExam(id, userAnswers);
            return ResponseEntity.ok(convertToDTO(result));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    
    @GetMapping("/attempts/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExamAttemptDTO>> getMyAttempts() {
        // Get user ID from authentication context
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ExamAttempt> attempts = examAttemptService.getAttemptsByUserId(userId);
        List<ExamAttemptDTO> attemptDTOs = attempts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attemptDTOs);
    }
    
    @GetMapping("/attempts/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<ExamAttemptDTO>> getAttemptsByUserId(@PathVariable String userId) {
        List<ExamAttempt> attempts = examAttemptService.getAttemptsByUserId(userId);
        List<ExamAttemptDTO> attemptDTOs = attempts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attemptDTOs);
    }
    
    @GetMapping("/attempts/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExamAttemptDTO> getAttemptById(@PathVariable Long id) {
        return examAttemptService.getAttemptById(id)
                .map(attempt -> ResponseEntity.ok(convertToDTO(attempt)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam attempt not found"));
    }
    
    @GetMapping("/{examId}/attempts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExamAttemptDTO>> getAttemptsByExamIdAndUserId(
            @PathVariable Long examId) {
        // Get user ID from authentication context
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ExamAttempt> attempts = examAttemptService.getAttemptsByExamIdAndUserId(examId, userId);
        List<ExamAttemptDTO> attemptDTOs = attempts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attemptDTOs);
    }
    
    // New endpoint for flagging a question
    @PostMapping("/attempts/{attemptId}/flag/{questionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExamAttemptDTO> flagQuestion(
            @PathVariable Long attemptId,
            @PathVariable Long questionId) {
        try {
            ExamAttempt attempt = examAttemptService.flagQuestion(attemptId, questionId);
            return ResponseEntity.ok(convertToDTO(attempt));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    
    // New endpoint for unflagging a question
    @DeleteMapping("/attempts/{attemptId}/flag/{questionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExamAttemptDTO> unflagQuestion(
            @PathVariable Long attemptId,
            @PathVariable Long questionId) {
        try {
            ExamAttempt attempt = examAttemptService.unflagQuestion(attemptId, questionId);
            return ResponseEntity.ok(convertToDTO(attempt));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    
    // New endpoint for getting all flagged questions for an attempt
    @GetMapping("/attempts/{attemptId}/flags")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FlaggedQuestionDTO>> getFlaggedQuestions(
            @PathVariable Long attemptId) {
        try {
            List<FlaggedQuestion> flaggedQuestions = examAttemptService.getFlaggedQuestions(attemptId);
            List<FlaggedQuestionDTO> dtos = flaggedQuestions.stream()
                    .map(fq -> new FlaggedQuestionDTO(fq.getAttempt().getId(), fq.getQuestion().getId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    
    private ExamAttemptDTO convertToDTO(ExamAttempt attempt) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        
        ExamAttemptDTO dto = new ExamAttemptDTO();
        dto.setId(attempt.getId());
        dto.setExamId(attempt.getExam().getId());
        dto.setUserId(attempt.getUserId());
        dto.setStartTime(attempt.getStartTime().format(formatter));
        dto.setStatus(attempt.getStatus().toString());
        
        dto.setAnswers(attempt.getAnswers().stream()
                .map(answer -> {
                    UserAnswerDTO answerDTO = new UserAnswerDTO();
                    answerDTO.setQuestionId(answer.getQuestion().getId());
                    answerDTO.setSelectedOptionId(answer.getSelectedOptionId());
                    answerDTO.setTimeSpent(answer.getTimeSpent());
                    return answerDTO;
                })
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    private UserAnswer convertToEntity(UserAnswerDTO dto) {
        UserAnswer userAnswer = new UserAnswer();
        
        Question question = questionService.getQuestionById(dto.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + dto.getQuestionId()));
        
        userAnswer.setQuestion(question);
        userAnswer.setSelectedOptionId(dto.getSelectedOptionId());
        userAnswer.setTimeSpent(dto.getTimeSpent());
        
        return userAnswer;
    }
    
    private ExamResultDTO convertToDTO(ExamResult result) {
        ExamResultDTO dto = new ExamResultDTO();
        dto.setExamId(result.getAttempt().getExam().getId());
        dto.setExamTitle(result.getAttempt().getExam().getTitle());
        dto.setScore(result.getScore());
        dto.setTotalMarks(result.getAttempt().getExam().getTotalMarks());
        dto.setPassingMarks(result.getAttempt().getExam().getPassingMarks());
        dto.setIsPassed(result.getIsPassed());
        dto.setTimeSpent(result.getTimeSpent());
        
        List<ExamResultDTO.QuestionResultDTO> questionResults = new ArrayList<>();
        
        // For each answer in the attempt, create a question result
        for (UserAnswer answer : result.getAttempt().getAnswers()) {
            Question question = answer.getQuestion();
            String correctOptionId = question.getCorrectAnswer();
            boolean isCorrect = correctOptionId.equals(answer.getSelectedOptionId());
            
            ExamResultDTO.QuestionResultDTO questionResult = new ExamResultDTO.QuestionResultDTO();
            questionResult.setQuestionId(question.getId());
            questionResult.setQuestionText(question.getQuestionText());
            questionResult.setUserAnswerId(answer.getSelectedOptionId());
            questionResult.setCorrectAnswerId(correctOptionId);
            questionResult.setIsCorrect(isCorrect);
            questionResult.setExplanation(question.getExplanation());
            questionResult.setPoints(question.getMarks());
            questionResult.setEarnedPoints(isCorrect ? question.getMarks() : 0);
            
            questionResults.add(questionResult);
        }
        
        dto.setQuestionResults(questionResults);
        
        return dto;
    }
}
