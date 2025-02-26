package com.pharmacyhub.domain.repository;

import com.pharmacyhub.domain.entity.ExamPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamPaperRepository extends JpaRepository<ExamPaper, Long> {
    
    @Query("SELECT p FROM ExamPaper p WHERE p.deleted = false")
    List<ExamPaper> findAllActive();
    
    @Query("SELECT p FROM ExamPaper p WHERE p.deleted = false AND p.type = 'MODEL'")
    List<ExamPaper> findAllModelPapers();
    
    @Query("SELECT p FROM ExamPaper p WHERE p.deleted = false AND p.type = 'PAST'")
    List<ExamPaper> findAllPastPapers();
    
    @Query("SELECT p FROM ExamPaper p WHERE p.deleted = false AND p.id = :id")
    Optional<ExamPaper> findByIdAndNotDeleted(Long id);
    
    @Query("SELECT COUNT(p) > 0 FROM ExamPaper p WHERE p.title = :title AND p.deleted = false")
    boolean existsByTitle(String title);
    
    @Query("SELECT AVG(p.durationMinutes) FROM ExamPaper p WHERE p.deleted = false")
    Double getAverageDuration();
    
    @Query("SELECT COUNT(p) FROM ExamPaper p WHERE p.deleted = false")
    Integer getTotalPaperCount();
}
