package com.pharmacyhub.deprecated.entity;

// DEPRECATED: This entity is redundant with Exam entity
// Use Exam entity for all new development

import com.pharmacyhub.domain.entity.Exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "exam_papers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamPaper {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Difficulty is required")
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    
    @NotNull(message = "Question count is required")
    @PositiveOrZero(message = "Question count must be positive or zero")
    @Column(name = "question_count")
    private Integer questionCount;
    
    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration must be positive or zero")
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @ElementCollection
    @CollectionTable(name = "exam_paper_tags", joinColumns = @JoinColumn(name = "exam_paper_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();
    
    @NotNull(message = "Premium status is required")
    @Column(name = "is_premium")
    private Boolean premium = false;
    
    @Column(name = "attempt_count")
    private Integer attemptCount = 0;
    
    @Column(name = "success_rate_percent")
    private Double successRatePercent = 0.0;
    
    @Column(name = "last_updated_date")
    private LocalDate lastUpdatedDate = LocalDate.now();
    
    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    private PaperType type;
    
    @Column(nullable = false)
    private boolean deleted = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    private Exam exam;
    
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
    
    public enum PaperType {
        MODEL, PAST
    }
}
