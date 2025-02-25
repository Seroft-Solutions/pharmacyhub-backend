package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.Exam;
import java.util.List;
import java.util.Optional;

public interface ExamService {
    List<Exam> getAllExams();
    
    List<Exam> getAllPublishedExams();
    
    Optional<Exam> getExamById(Long id);
    
    Exam createExam(Exam exam);
    
    Exam updateExam(Long id, Exam exam);
    
    void deleteExam(Long id);
    
    List<Exam> getExamsByStatus(Exam.ExamStatus status);
    
    boolean existsByTitle(String title);
    
    Exam publishExam(Long id);
    
    Exam archiveExam(Long id);
}
