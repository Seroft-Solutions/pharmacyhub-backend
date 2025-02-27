package com.pharmacyhub.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
public class Option {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @NotBlank(message = "Option text is required")
    @Column(name = "option_text", columnDefinition = "TEXT")
    private String text;

    @NotBlank(message = "Option label is required")
    @Column(name = "option_label", length = 1)
    private String label;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect = false;

    @Column(nullable = false)
    private boolean deleted = false;

    // Helper method to get label for front-end
    public String getOptionLabel() {
        return this.label;
    }

    // Helper method to get text for front-end
    public String getOptionText() {
        return this.text;
    }

    // Helper method to set label
    public void setOptionLabel(String label) {
        this.label = label;
    }

    // Helper method to set text
    public void setOptionText(String text) {
        this.text = text;
    }

    // Explicit getter and setter for isCorrect to match expected method names
    public boolean getIsCorrect() {
        return this.isCorrect;
    }

    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
}
