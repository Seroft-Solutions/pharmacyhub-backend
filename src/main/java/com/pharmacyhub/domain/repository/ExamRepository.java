package com.pharmacyhub.domain.repository;

import com.pharmacyhub.domain.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long>
{
    @Query("SELECT e FROM Exam e WHERE e.deleted = false")
    List<Exam> findAllActive();

    @Query("SELECT e FROM Exam e WHERE e.deleted = false AND e.status = 'PUBLISHED'")
    List<Exam> findAllPublished();

    @Query("SELECT e FROM Exam e WHERE e.deleted = false AND e.id = :id")
    Optional<Exam> findByIdAndNotDeleted(Long id);

    @Query("SELECT e FROM Exam e WHERE e.deleted = false AND e.status = :status")
    List<Exam> findByStatus(Exam.ExamStatus status);

    @Query("SELECT e FROM Exam e WHERE e.deleted = false AND e.id = :id AND e.status = :status")
    Optional<Exam> findByIdAndStatus(Long id, Exam.ExamStatus status);

    @Query("SELECT COUNT(e) > 0 FROM Exam e WHERE e.title = :title AND e.deleted = false")
    boolean existsByTitle(String title);
}
