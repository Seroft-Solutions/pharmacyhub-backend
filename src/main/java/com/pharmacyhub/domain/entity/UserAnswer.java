package com.pharmacyhub.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private ExamAttempt attempt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @Column(name = "selected_option_id", length = 1)
    private String selectedOptionId;
    
    @NotNull(message = "Time spent is required")
    @PositiveOrZero(message = "Time spent must be positive or zero")
    @Column(name = "time_spent")
    private Integer timeSpent = 0; // in seconds
    
    @Column(nullable = false)
    private boolean deleted = false;
}
