package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.ExamPaper;
import com.pharmacyhub.dto.ExamStatsDTO;

import java.util.List;
import java.util.Optional;

public interface ExamPaperService {
    
    List<ExamPaper> getAllPapers();
    
    List<ExamPaper> getModelPapers();
    
    List<ExamPaper> getPastPapers();
    
    Optional<ExamPaper> getPaperById(Long id);
    
    ExamPaper createPaper(ExamPaper paper);
    
    ExamPaper updatePaper(Long id, ExamPaper paper);
    
    void deletePaper(Long id);
    
    boolean existsByTitle(String title);
    
    ExamStatsDTO getExamStats();
}
