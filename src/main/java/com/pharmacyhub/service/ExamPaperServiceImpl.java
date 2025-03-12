package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.ExamPaper;
import com.pharmacyhub.domain.repository.ExamAttemptRepository;
import com.pharmacyhub.domain.repository.ExamPaperRepository;
import com.pharmacyhub.domain.repository.ExamResultRepository;
import com.pharmacyhub.dto.ExamStatsDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExamPaperServiceImpl implements ExamPaperService {
    
    private final ExamPaperRepository examPaperRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamResultRepository examResultRepository;
    
    public ExamPaperServiceImpl(
            ExamPaperRepository examPaperRepository,
            ExamAttemptRepository examAttemptRepository,
            ExamResultRepository examResultRepository) {
        this.examPaperRepository = examPaperRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.examResultRepository = examResultRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamPaper> getAllPapers() {
        return examPaperRepository.findAllActive();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamPaper> getModelPapers() {
        return examPaperRepository.findAllModelPapers();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExamPaper> getPastPapers() {
        return examPaperRepository.findAllPastPapers();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ExamPaper> getPaperById(Long id) {
        return examPaperRepository.findByIdAndNotDeleted(id);
    }
    
    @Override
    public ExamPaper createPaper(ExamPaper paper) {
        if (examPaperRepository.existsByTitle(paper.getTitle())) {
            throw new IllegalArgumentException("An exam paper with this title already exists");
        }
        return examPaperRepository.save(paper);
    }
    
    @Override
    public ExamPaper updatePaper(Long id, ExamPaper paper) {
        ExamPaper existingPaper = examPaperRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam paper not found with id: " + id));
        
        if (!existingPaper.getTitle().equals(paper.getTitle()) &&
                examPaperRepository.existsByTitle(paper.getTitle())) {
            throw new IllegalArgumentException("An exam paper with this title already exists");
        }
        
        existingPaper.setTitle(paper.getTitle());
        existingPaper.setDescription(paper.getDescription());
        existingPaper.setDifficulty(paper.getDifficulty());
        existingPaper.setQuestionCount(paper.getQuestionCount());
        existingPaper.setDurationMinutes(paper.getDurationMinutes());
        existingPaper.setTags(paper.getTags());
        existingPaper.setPremium(paper.getPremium());
        existingPaper.setType(paper.getType());
        existingPaper.setExam(paper.getExam());
        
        return examPaperRepository.save(existingPaper);
    }
    
    @Override
    public void deletePaper(Long id) {
        ExamPaper paper = examPaperRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam paper not found with id: " + id));
        paper.setDeleted(true);
        examPaperRepository.save(paper);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByTitle(String title) {
        return examPaperRepository.existsByTitle(title);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExamStatsDTO getExamStats() {
        // Convert Long to Integer
        Long totalPapersLong = examPaperRepository.getTotalPaperCount();
        Integer totalPapers = totalPapersLong != null ? totalPapersLong.intValue() : 0;
        
        // Convert Double to Integer
        Double avgDurationDouble = examPaperRepository.getAverageDuration();
        Integer avgDuration = avgDurationDouble != null ? avgDurationDouble.intValue() : 0;
        
        Double completionRate = examResultRepository.getAverageCompletionRate();
        Integer completionRatePercent = completionRate != null ? 
                (int) (completionRate * 100) : 0;
        
        // Active users in the last 30 days - convert long to Integer
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long activeUsersLong = examAttemptRepository.countUniqueUsersSince(thirtyDaysAgo);
        Integer activeUsers = (int) activeUsersLong;
        
        return ExamStatsDTO.builder()
                .totalPapers(totalPapers)
                .avgDuration(avgDuration)
                .completionRate(completionRatePercent)
                .activeUsers(activeUsers)
                .build();
    }
}
