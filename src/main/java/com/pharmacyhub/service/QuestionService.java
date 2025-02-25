package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.Question;

import java.util.List;
import java.util.Optional;

public interface QuestionService {
    List<Question> getQuestionsByExamId(Long examId);
    Optional<Question> getQuestionById(Long id);
    Question createQuestion(Question question);
    Question updateQuestion(Long id, Question question);
    void deleteQuestion(Long id);
    Integer getMaxQuestionNumberByExamId(Long examId);
    Long countQuestionsByExamId(Long examId);
}
