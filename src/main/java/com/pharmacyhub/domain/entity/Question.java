package com.pharmacyhub.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @NotNull(message = "Question number is required")
    @Column(name = "question_number")
    private Integer questionNumber;

    @NotBlank(message = "Question text is required")
    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Option> options = new ArrayList<>();

    @NotBlank(message = "Correct answer is required")
    @Column(name = "correct_answer", length = 1)
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @PositiveOrZero(message = "Marks must be positive or zero")
    private Integer marks = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType type = QuestionType.MCQ;

    @Column(nullable = false)
    private boolean deleted = false;

    // Helper method to add option
    public void addOption(Option option)
    {
        options.add(option);
        option.setQuestion(this);
    }

    // Helper method to remove option
    public void removeOption(Option option)
    {
        options.remove(option);
        option.setQuestion(null);
    }

    public enum QuestionType
    {
        MCQ,
        TRUE_FALSE
    }
}
