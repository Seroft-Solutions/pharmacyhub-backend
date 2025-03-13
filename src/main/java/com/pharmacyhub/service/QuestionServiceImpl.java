package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.Question;
import com.pharmacyhub.domain.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {
    
    private final QuestionRepository questionRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> getQuestionsByExamId(Long examId) {
        return questionRepository.findByExamId(examId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findByIdAndNotDeleted(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Question> findById(Long id) {
        return questionRepository.findByIdAndNotDeleted(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Question> findByExamId(Long examId) {
        return questionRepository.findByExamId(examId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Question> findByTopic(String topic) {
        return questionRepository.findByTopic(topic);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Question> findByDifficulty(String difficulty) {
        return questionRepository.findByDifficulty(difficulty);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Question> findRandom(int count, String topic, String difficulty) {
        return questionRepository.findRandom(count, topic, difficulty);
    }

    @Override
    public Question createQuestion(Question question) {
        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public Question updateQuestion(Long id, Question questionDetails) {
        // 1. Find the existing question
        Question question = questionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + id));

        // 2. Update basic question properties
        if (questionDetails.getQuestionText() != null) {
            question.setQuestionText(questionDetails.getQuestionText());
        }
        if (questionDetails.getCorrectAnswer() != null) {
            question.setCorrectAnswer(questionDetails.getCorrectAnswer());
        }
        if (questionDetails.getExplanation() != null) {
            question.setExplanation(questionDetails.getExplanation());
        }
        if (questionDetails.getMarks() != null) {
            question.setMarks(questionDetails.getMarks());
        }
        if (questionDetails.getType() != null) {
            question.setType(questionDetails.getType());
        }
        if (questionDetails.getTopic() != null) {
            question.setTopic(questionDetails.getTopic());
        }
        if (questionDetails.getDifficulty() != null) {
            question.setDifficulty(questionDetails.getDifficulty());
        }
        
        // 3. Update options if provided
        if (questionDetails.getOptions() != null && !questionDetails.getOptions().isEmpty()) {
            // Update existing options
            questionDetails.getOptions().forEach(updatedOption -> {
                // If the option has an ID, it's an existing option to update
                if (updatedOption.getId() != null) {
                    question.getOptions().stream()
                        .filter(o -> o.getId().equals(updatedOption.getId()))
                        .findFirst()
                        .ifPresent(existingOption -> {
                            // Update option text if provided
                            if (updatedOption.getText() != null) {
                                existingOption.setText(updatedOption.getText());
                            }
                            
                            // Update option label if provided
                            if (updatedOption.getLabel() != null) {
                                existingOption.setLabel(updatedOption.getLabel());
                            }
                        });
                }
            });
            
            // 4. Mark all options as correct/incorrect based on correctAnswer
            if (question.getCorrectAnswer() != null) {
                question.getOptions().forEach(option -> {
                    option.setIsCorrect(question.getCorrectAnswer().equals(option.getLabel()));
                });
            }
        }

        // 5. Save and return the updated question
        return questionRepository.save(question);
    }

    @Override
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + id));
        question.setDeleted(true);
        questionRepository.save(question);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getMaxQuestionNumberByExamId(Long examId) {
        return questionRepository.findMaxQuestionNumberByExamId(examId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countQuestionsByExamId(Long examId) {
        return questionRepository.countByExamId(examId);
    }
}