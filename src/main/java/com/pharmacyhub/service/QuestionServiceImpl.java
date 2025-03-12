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
    public Question updateQuestion(Long id, Question questionDetails) {
        Question question = questionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + id));

        question.setQuestionText(questionDetails.getQuestionText());
        question.setCorrectAnswer(questionDetails.getCorrectAnswer());
        question.setExplanation(questionDetails.getExplanation());
        question.setMarks(questionDetails.getMarks());
        question.setType(questionDetails.getType());

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