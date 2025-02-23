package com.pharmacy.hub.features.exam.domain.repository;

import com.pharmacy.hub.features.exam.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>
{

    @Query("SELECT q FROM Question q WHERE q.exam.id = :examId AND q.isDeleted = false ORDER BY q.questionNumber")
    List<Question> findByExamId(Long examId);

    @Query("SELECT q FROM Question q WHERE q.isDeleted = false AND q.id = :id")
    Optional<Question> findByIdAndNotDeleted(Long id);

    @Query("SELECT COALESCE(MAX(q.questionNumber), 0) FROM Question q WHERE q.exam.id = :examId")
    Integer findMaxQuestionNumberByExamId(Long examId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.exam.id = :examId AND q.isDeleted = false")
    Long countByExamId(Long examId);
}
