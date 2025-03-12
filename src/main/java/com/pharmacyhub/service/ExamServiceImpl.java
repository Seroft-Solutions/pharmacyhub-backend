package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.domain.entity.ExamAttempt;
import com.pharmacyhub.domain.entity.Question;
import com.pharmacyhub.domain.repository.ExamAttemptRepository;
import com.pharmacyhub.domain.repository.ExamRepository;
import com.pharmacyhub.domain.repository.ExamResultRepository;
import com.pharmacyhub.domain.repository.QuestionRepository;
import com.pharmacyhub.dto.request.ExamFilterRequestDTO;
import com.pharmacyhub.dto.response.ExamResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExamServiceImpl implements ExamService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExamServiceImpl.class);
    
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamResultRepository examResultRepository;
    
    @Autowired
    public ExamServiceImpl(
            ExamRepository examRepository, 
            QuestionRepository questionRepository,
            ExamAttemptRepository examAttemptRepository,
            ExamResultRepository examResultRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.examResultRepository = examResultRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Exam> findAllActive() {
        return examRepository.findByDeletedFalse();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Exam> findAllPublished() {
        return examRepository.findByStatusAndDeletedFalse(Exam.ExamStatus.PUBLISHED);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Exam> findById(Long id) {
        return examRepository.findByIdAndDeletedFalse(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Exam> findByStatus(Exam.ExamStatus status) {
        return examRepository.findByStatusAndDeletedFalse(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByTitle(String title) {
        return examRepository.existsByTitle(title);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamResponseDTO> findPapersByFilter(ExamFilterRequestDTO filters) {
        logger.info("Finding papers with filters: {}", filters);
        
        List<Exam> exams;
        
        if (filters.getType() != null) {
            // Filter by paper type
            String paperType = filters.getType().toUpperCase();
            // Implement custom filtering based on paper type
            exams = examRepository.findByTagsContainingAndDeletedFalse(paperType);
        } else {
            // No specific type, get all published exams
            exams = findAllPublished();
        }
        
        // Apply difficulty filter if specified
        if (filters.getDifficulty() != null && !filters.getDifficulty().isEmpty()) {
            exams = exams.stream()
                    .filter(exam -> hasTag(exam, filters.getDifficulty()))
                    .collect(Collectors.toList());
        }
        
        // Apply topic/subject filter if specified
        if (filters.getTopic() != null && !filters.getTopic().isEmpty()) {
            exams = exams.stream()
                    .filter(exam -> hasTag(exam, filters.getTopic()))
                    .collect(Collectors.toList());
        }
        
        // Convert to DTOs and return
        return exams.stream()
                .map(this::mapToExamResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExamResponseDTO findPaperById(Long id) {
        Exam exam = examRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Paper not found with id: " + id));
        
        return mapToExamResponseDTO(exam);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getExamStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Count total papers
        long totalPapers = examRepository.countByStatusAndDeletedFalse(Exam.ExamStatus.PUBLISHED);
        stats.put("totalPapers", totalPapers);
        
        // Get average duration
        Double avgDuration = examRepository.getAverageDuration();
        stats.put("avgDuration", avgDuration != null ? avgDuration : 0);
        
        // Get completion rate
        Double completionRate = examResultRepository.getAverageCompletionRate();
        stats.put("completionRate", completionRate != null ? completionRate : 0);
        
        // Count active users (users with at least one attempt)
        long activeUsers = examAttemptRepository.countDistinctUserIds();
        stats.put("activeUsers", activeUsers);
        
        return stats;
    }
    
    @Override
    public Exam createExam(Exam exam) {
        // Validate exam
        validateExam(exam);
        
        if (examRepository.existsByTitle(exam.getTitle())) {
            throw new IllegalArgumentException("An exam with this title already exists");
        }
        
        exam.setStatus(Exam.ExamStatus.DRAFT);
        
        // Save the exam
        Exam savedExam = examRepository.save(exam);
        
        // Save questions if provided
        if (exam.getQuestions() != null && !exam.getQuestions().isEmpty()) {
            for (Question question : exam.getQuestions()) {
                question.setExam(savedExam);
            }
            questionRepository.saveAll(exam.getQuestions());
        }
        
        return savedExam;
    }
    
    @Override
    public Exam updateExam(Long id, Exam exam) {
        // Find existing exam
        Exam existingExam = examRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with id: " + id));
        
        // Validate exam
        validateExam(exam);
        
        if (!existingExam.getTitle().equals(exam.getTitle()) && 
            examRepository.existsByTitle(exam.getTitle())) {
            throw new IllegalArgumentException("An exam with this title already exists");
        }
        
        // Update basic properties
        existingExam.setTitle(exam.getTitle());
        existingExam.setDescription(exam.getDescription());
        existingExam.setDuration(exam.getDuration());
        existingExam.setTotalMarks(exam.getTotalMarks());
        existingExam.setPassingMarks(exam.getPassingMarks());
        existingExam.setStatus(exam.getStatus());
        
        // Save the updated exam
        Exam updatedExam = examRepository.save(existingExam);
        
        // Update questions if provided
        if (exam.getQuestions() != null) {
            // Get existing questions
            List<Question> existingQuestions = questionRepository.findByExamIdAndDeletedFalse(id);
            
            // Mark all existing questions as deleted
            for (Question question : existingQuestions) {
                question.setDeleted(true);
            }
            questionRepository.saveAll(existingQuestions);
            
            // Add new questions
            for (Question question : exam.getQuestions()) {
                question.setExam(updatedExam);
                question.setId(null); // Ensure new questions are created
            }
            questionRepository.saveAll(exam.getQuestions());
        }
        
        return updatedExam;
    }
    
    @Override
    public void deleteExam(Long id) {
        Exam exam = examRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with id: " + id));
        
        // Soft delete
        exam.setDeleted(true);
        examRepository.save(exam);
        
        // Soft delete all questions
        List<Question> questions = questionRepository.findByExamIdAndDeletedFalse(id);
        for (Question question : questions) {
            question.setDeleted(true);
        }
        questionRepository.saveAll(questions);
    }
    
    @Override
    public Exam publishExam(Long id) {
        Exam exam = examRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with id: " + id));
        
        // Check if the exam has questions
        List<Question> questions = questionRepository.findByExamIdAndDeletedFalse(id);
        if (questions.isEmpty()) {
            throw new IllegalStateException("Cannot publish an exam without questions");
        }
        
        // Update status
        exam.setStatus(Exam.ExamStatus.PUBLISHED);
        return examRepository.save(exam);
    }
    
    @Override
    public Exam archiveExam(Long id) {
        Exam exam = examRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with id: " + id));
        
        // Update status
        exam.setStatus(Exam.ExamStatus.ARCHIVED);
        return examRepository.save(exam);
    }
    
    /**
     * Map exam entity to response DTO
     */
    private ExamResponseDTO mapToExamResponseDTO(Exam exam) {
        ExamResponseDTO dto = new ExamResponseDTO();
        dto.setId(exam.getId());
        dto.setTitle(exam.getTitle());
        dto.setDescription(exam.getDescription());
        dto.setDuration(exam.getDuration());
        dto.setTotalMarks(exam.getTotalMarks());
        dto.setPassingMarks(exam.getPassingMarks());
        dto.setStatus(exam.getStatus());
        
        // Get question count
        int questionCount = questionRepository.countByExamIdAndDeletedFalse(exam.getId());
        dto.setAttemptCount(questionCount);
        
        // Get exam statistics
        Long attemptCount = examAttemptRepository.countByExamIdAndStatusAndDeletedFalse(
                exam.getId(), ExamAttempt.AttemptStatus.COMPLETED);
        dto.setAttemptCount(attemptCount != null ? attemptCount.intValue() : 0);
        
        // Get success rate
        Double successRate = examResultRepository.getSuccessRateByExamId(exam.getId());
        dto.setAverageScore(successRate != null ? successRate : 0.0);
        
        // Get tags
        List<String> tags = new ArrayList<>();
        if (exam.getTags() != null) {
            tags.addAll(exam.getTags());
        }
        
        return dto;
    }
    
    /**
     * Extract paper type from exam tags
     */
    private String getPaperType(Exam exam) {
        if (exam.getTags() == null) {
            return "PRACTICE"; // Default type
        }
        
        for (String tag : exam.getTags()) {
            if (tag.equalsIgnoreCase("MODEL")) {
                return "MODEL";
            } else if (tag.equalsIgnoreCase("PAST")) {
                return "PAST";
            } else if (tag.equalsIgnoreCase("SUBJECT")) {
                return "SUBJECT";
            }
        }
        
        return "PRACTICE"; // Default type
    }
    
    /**
     * Check if exam has a specific tag
     */
    private boolean hasTag(Exam exam, String tag) {
        if (exam.getTags() == null) {
            return false;
        }
        
        return exam.getTags().stream()
                .anyMatch(t -> t.equalsIgnoreCase(tag));
    }
    
    /**
     * Validate exam
     */
    private void validateExam(Exam exam) {
        if (exam.getTitle() == null || exam.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Exam title is required");
        }
        
        if (exam.getDuration() == null || exam.getDuration() < 0) {
            throw new IllegalArgumentException("Exam duration must be positive or zero");
        }
        
        if (exam.getTotalMarks() == null || exam.getTotalMarks() < 0) {
            throw new IllegalArgumentException("Total marks must be positive or zero");
        }
        
        if (exam.getPassingMarks() == null || exam.getPassingMarks() < 0) {
            throw new IllegalArgumentException("Passing marks must be positive or zero");
        }
        
        if (exam.getPassingMarks() > exam.getTotalMarks()) {
            throw new IllegalArgumentException("Passing marks cannot be greater than total marks");
        }
    }
}