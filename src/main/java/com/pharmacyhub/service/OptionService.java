package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.Option;
import java.util.List;
import java.util.Optional;

public interface OptionService {
    List<Option> getOptionsByQuestionId(Long questionId);
    Optional<Option> getOptionById(Long id);
    Option createOption(Option option);
    Option updateOption(Long id, Option option);
    void deleteOption(Long id);
    Optional<Option> getCorrectOptionByQuestionId(Long questionId);
    Long countOptionsByQuestionId(Long questionId);
    boolean existsByQuestionIdAndLabel(Long questionId, String label);
}