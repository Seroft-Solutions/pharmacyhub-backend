package com.pharmacyhub.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private ExamAttempt attempt;
    
    @NotNull(message = "Score is required")
    private Double score;
    
    @NotNull(message = "Total questions is required")
    @Column(name = "total_questions")
    private Integer totalQuestions;
    
    @NotNull(message = "Correct answers is required")
    @Column(name = "correct_answers")
    private Integer correctAnswers;
    
    @NotNull(message = "Incorrect answers is required")
    @Column(name = "incorrect_answers")
    private Integer incorrectAnswers;
    
    @NotNull(message = "Unanswered questions is required")
    @Column(name = "unanswered")
    private Integer unanswered;
    
    @NotNull(message = "Time spent is required")
    @Column(name = "time_spent")
    private Integer timeSpent; // in seconds
    
    @NotNull(message = "Passing status is required")
    @Column(name = "is_passed")
    private Boolean isPassed;
    
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private boolean deleted = false;
}
