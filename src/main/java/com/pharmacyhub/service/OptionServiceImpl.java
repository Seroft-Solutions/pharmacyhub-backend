package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.Option;
import com.pharmacyhub.domain.repository.OptionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OptionServiceImpl implements OptionService {

    private final OptionRepository optionRepository;

    public OptionServiceImpl(OptionRepository optionRepository) {
        this.optionRepository = optionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Option> getOptionsByQuestionId(Long questionId) {
        return optionRepository.findByQuestionId(questionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Option> getOptionById(Long id) {
        return optionRepository.findByIdAndNotDeleted(id);
    }

    @Override
    public Option createOption(Option option) {
        return optionRepository.save(option);
    }

    @Override
    public Option updateOption(Long id, Option optionDetails) {
        Option option = optionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Option not found with id: " + id));

        option.setLabel(optionDetails.getLabel());
        option.setText(optionDetails.getText());
        option.setIsCorrect(optionDetails.getIsCorrect());

        return optionRepository.save(option);
    }

    @Override
    public void deleteOption(Long id) {
        Option option = optionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Option not found with id: " + id));
        option.setDeleted(true);
        optionRepository.save(option);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Option> getCorrectOptionByQuestionId(Long questionId) {
        return optionRepository.findCorrectOptionByQuestionId(questionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOptionsByQuestionId(Long questionId) {
        return optionRepository.countByQuestionId(questionId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByQuestionIdAndLabel(Long questionId, String label) {
        return optionRepository.existsByQuestionIdAndLabel(questionId, label);
    }
}