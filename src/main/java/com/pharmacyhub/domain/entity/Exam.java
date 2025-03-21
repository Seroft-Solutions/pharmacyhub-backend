package com.pharmacyhub.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration must be positive or zero")
    private Integer duration; // in minutes

    @NotNull(message = "Total marks is required")
    @PositiveOrZero(message = "Total marks must be positive or zero")
    @Column(name = "total_marks")
    private Integer totalMarks;

    @NotNull(message = "Passing marks is required")
    @PositiveOrZero(message = "Passing marks must be positive or zero")
    @Column(name = "passing_marks")
    private Integer passingMarks;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "exam_tags", joinColumns = @JoinColumn(name = "exam_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamStatus status = ExamStatus.DRAFT;

    @Column(nullable = false)
    private boolean deleted = false;
    
    // New fields for premium exams
    @Column(nullable = false)
    private boolean premium = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;
    
    @Column(name = "custom_price", nullable = false)
    private boolean customPrice = false;

    // Helper method to add question
    public void addQuestion(Question question) {
        questions.add(question);
        question.setExam(this);
    }

    // Helper method to remove question
    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setExam(null);
    }
    
    // Helper method to add tag
    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }
    
    // Helper method to remove tag
    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }

    public enum ExamStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }
}