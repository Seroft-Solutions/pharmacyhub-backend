package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.domain.repository.ExamRepository;
import com.pharmacyhub.dto.ExamDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExamServiceImpl implements ExamService {
    
    private final ExamRepository examRepository;

    public ExamServiceImpl(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exam> findAllActive() {
        return examRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exam> findAllPublished() {
        return examRepository.findAllPublished();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Exam> findById(Long id) {
        return examRepository.findByIdAndNotDeleted(id);
    }

    @Override
    public Exam createExam(Exam exam) {
        if (examRepository.existsByTitle(exam.getTitle())) {
            throw new IllegalArgumentException("An exam with this title already exists");
        }
        exam.setStatus(Exam.ExamStatus.DRAFT);
        return examRepository.save(exam);
    }

    @Override
    public Exam updateExam(Long id, Exam exam) {
        Exam existingExam = examRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with id: " + id));
        
        if (!existingExam.getTitle().equals(exam.getTitle()) && 
            examRepository.existsByTitle(exam.getTitle())) {
            throw new IllegalArgumentException("An exam with this title already exists");
        }

        existingExam.setTitle(exam.getTitle());
        existingExam.setDescription(exam.getDescription());
        existingExam.setDuration(exam.getDuration());
        existingExam.setTotalMarks(exam.getTotalMarks());
        existingExam.setPassingMarks(exam.getPassingMarks());
        
        return examRepository.save(existingExam);
    }

    @Override
    public void deleteExam(Long id) {
        Exam exam = examRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with id: " + id));
        exam.setDeleted(true);
        examRepository.save(exam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exam> findByStatus(Exam.ExamStatus status) {
        return examRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTitle(String title) {
        return examRepository.existsByTitle(title);
    }

    @Override
    public Exam publishExam(Long id) {
        Exam exam = examRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with id: " + id));
        
        if (exam.getQuestions().isEmpty()) {
            throw new IllegalStateException("Cannot publish an exam without questions");
        }
        
        exam.setStatus(Exam.ExamStatus.PUBLISHED);
        return examRepository.save(exam);
    }

    @Override
    public Exam archiveExam(Long id) {
        Exam exam = examRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found with id: " + id));
        exam.setStatus(Exam.ExamStatus.ARCHIVED);
        return examRepository.save(exam);
    }
}
