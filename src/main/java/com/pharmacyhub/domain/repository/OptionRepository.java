package com.pharmacyhub.domain.repository;

import com.pharmacyhub.domain.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long>
{

    @Query("SELECT o FROM Option o WHERE o.question.id = :questionId AND o.deleted = false ORDER BY o.label")
    List<Option> findByQuestionId(Long questionId);

    @Query("SELECT o FROM Option o WHERE o.deleted = false AND o.id = :id")
    Optional<Option> findByIdAndNotDeleted(Long id);

    @Query("SELECT o FROM Option o WHERE o.question.id = :questionId AND o.isCorrect = true AND o.deleted = false")
    Optional<Option> findCorrectOptionByQuestionId(Long questionId);

    @Query("SELECT COUNT(o) FROM Option o WHERE o.question.id = :questionId AND o.deleted = false")
    Long countByQuestionId(Long questionId);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Option o " +
            "WHERE o.question.id = :questionId AND o.label = :label AND o.deleted = false")
    boolean existsByQuestionIdAndLabel(Long questionId, String label);
}
