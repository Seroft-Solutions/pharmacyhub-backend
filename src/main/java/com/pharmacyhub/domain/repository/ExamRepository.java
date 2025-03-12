package com.pharmacyhub.domain.repository;

import com.pharmacyhub.domain.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    
    /**
     * Find all non-deleted exams
     */
    @Query("SELECT e FROM Exam e WHERE e.deleted = false")
    List<Exam> findByDeletedFalse();
    
    /**
     * Find all published and non-deleted exams
     */
    @Query("SELECT e FROM Exam e WHERE e.deleted = false AND e.status = ?1")
    List<Exam> findByStatusAndDeletedFalse(Exam.ExamStatus status);
    
    /**
     * Find a non-deleted exam by ID
     */
    @Query("SELECT e FROM Exam e WHERE e.deleted = false AND e.id = ?1")
    Optional<Exam> findByIdAndDeletedFalse(Long id);
    
    /**
     * Find a non-deleted exam by ID (aliased method)
     */
    @Query("SELECT e FROM Exam e WHERE e.deleted = false AND e.id = ?1")
    Optional<Exam> findByIdAndNotDeleted(Long id);
    
    /**
     * Find all non-deleted exams by status
     */
    @Query("SELECT e FROM Exam e WHERE e.deleted = false AND e.status = ?1")
    List<Exam> findByStatus(Exam.ExamStatus status);
    
    /**
     * Find a non-deleted exam by ID and status
     */
    @Query("SELECT e FROM Exam e WHERE e.deleted = false AND e.id = ?1 AND e.status = ?2")
    Optional<Exam> findByIdAndStatus(Long id, Exam.ExamStatus status);
    
    /**
     * Check if a non-deleted exam with the given title exists
     */
    @Query("SELECT COUNT(e) > 0 FROM Exam e WHERE e.title = ?1 AND e.deleted = false")
    boolean existsByTitle(String title);
    
    /**
     * Count exams by status
     */
    @Query("SELECT COUNT(e) FROM Exam e WHERE e.status = ?1 AND e.deleted = false")
    long countByStatusAndDeletedFalse(Exam.ExamStatus status);
    
    /**
     * Get average duration of all published exams
     */
    @Query("SELECT AVG(e.duration) FROM Exam e WHERE e.status = 'PUBLISHED' AND e.deleted = false")
    Double getAverageDuration();
    
    /**
     * Find all non-deleted exams that have a specific tag
     */
    @Query("SELECT e FROM Exam e JOIN e.tags t WHERE t = ?1 AND e.deleted = false")
    List<Exam> findByTagsContainingAndDeletedFalse(String tag);
    
    /**
     * Find all non-deleted exams that have all the given tags
     */
    @Query("SELECT e FROM Exam e JOIN e.tags t WHERE t IN ?1 AND e.deleted = false GROUP BY e HAVING COUNT(DISTINCT t) = ?2")
    List<Exam> findByTagsContainingAllAndDeletedFalse(List<String> tags, long tagCount);
    
    /**
     * Get active exams (all non-deleted)
     */
    @Query("SELECT e FROM Exam e WHERE e.deleted = false")
    List<Exam> findAllActive();
}