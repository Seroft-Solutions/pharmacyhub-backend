package com.pharmacyhub.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "score")
    private Double score;
    
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnswer> answers = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;
    
    @Column(nullable = false)
    private boolean deleted = false;
    
    // Helper method to add answer
    public void addAnswer(UserAnswer answer) {
        answers.add(answer);
        answer.setAttempt(this);
    }
    
    // Helper method to remove answer
    public void removeAnswer(UserAnswer answer) {
        answers.remove(answer);
        answer.setAttempt(null);
    }
    
    public enum AttemptStatus {
        IN_PROGRESS,
        COMPLETED,
        ABANDONED
    }
}
