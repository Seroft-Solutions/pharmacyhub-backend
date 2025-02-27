package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.dto.ExamDTO;

import java.util.List;
import java.util.Optional;

public interface ExamService {
    List<Exam> findAllActive();
    List<Exam> findAllPublished();
    Optional<Exam> findById(Long id);
    Exam createExam(Exam exam);
    Exam updateExam(Long id, Exam exam);
    void deleteExam(Long id);
    List<Exam> findByStatus(Exam.ExamStatus status);
    boolean existsByTitle(String title);
    Exam publishExam(Long id);
    Exam archiveExam(Long id);
}
