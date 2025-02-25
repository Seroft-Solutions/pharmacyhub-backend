package com.pharmacyhub.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Option
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @NotBlank(message = "Label is required")
    @Column(length = 1)
    private String label;

    @NotBlank(message = "Option text is required")
    @Column(name = "option_text", columnDefinition = "TEXT")
    private String text;

    @NotNull(message = "IsCorrect flag is required")
    @Column(name = "is_correct")
    private Boolean isCorrect = false;

    @Column(nullable = false)
    private boolean deleted = false;
}
