package com.pharmacyhub.config;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.domain.entity.Option;
import com.pharmacyhub.domain.entity.Question;
import com.pharmacyhub.dto.request.ExamRequestDTO;
import com.pharmacyhub.dto.response.ExamResponseDTO;
import com.pharmacyhub.utils.EntityMapper;
import org.springframework.stereotype.Component;
import org.modelmapper.Converter;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;

/**
 * Configuration for entity-DTO mapping for Exam-related entities
 * Enhances the standard ModelMapper with custom mappings for complex scenarios
 */
@Configuration
public class ExamMapperConfig {

    private final EntityMapper entityMapper;

    @Autowired
    public ExamMapperConfig(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }

    @PostConstruct
    public void configure() {
        configureExamToResponseMapping();
        configureRequestToExamMapping();
    }

    /**
     * Configure mapping from Exam entity to ExamResponseDTO
     */
    private void configureExamToResponseMapping() {
        entityMapper.getModelMapper().addMappings(new PropertyMap<Exam, ExamResponseDTO>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setTitle(source.getTitle());
                map().setDescription(source.getDescription());
                map().setDuration(source.getDuration());
                map().setTotalMarks(source.getTotalMarks());
                map().setPassingMarks(source.getPassingMarks());
                map().setStatus(source.getStatus());
            }
        });

        entityMapper.getModelMapper().addMappings(new PropertyMap<Question, ExamResponseDTO.QuestionDTO>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setQuestionNumber(source.getQuestionNumber());
                map().setText(source.getQuestionText());
                map().setCorrectAnswer(source.getCorrectAnswer());
                map().setExplanation(source.getExplanation());
                map().setMarks(source.getMarks());
            }
        });

        entityMapper.getModelMapper().addMappings(new PropertyMap<Option, ExamResponseDTO.OptionDTO>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setLabel(source.getLabel());
                map().setText(source.getText());
                map().setIsCorrect(source.getIsCorrect());
            }
        });
    }

    /**
     * Configure mapping from ExamRequestDTO to Exam entity
     */
    private void configureRequestToExamMapping() {
        // Create a converter that handles the complex relationship between questions and exams
        Converter<ExamRequestDTO, Exam> examConverter = context -> {
            ExamRequestDTO source = context.getSource();
            Exam destination = context.getDestination();
            
            destination.setId(source.getId());
            destination.setTitle(source.getTitle());
            destination.setDescription(source.getDescription());
            destination.setDuration(source.getDuration());
            destination.setTotalMarks(source.getTotalMarks());
            destination.setPassingMarks(source.getPassingMarks());
            destination.setStatus(source.getStatus());
            
            if (destination.getQuestions() == null) {
                destination.setQuestions(new ArrayList<>());
            } else {
                destination.getQuestions().clear();
            }
            
            // Map questions
            if (source.getQuestions() != null) {
                for (ExamRequestDTO.QuestionDTO questionDTO : source.getQuestions()) {
                    Question question = new Question();
                    question.setId(questionDTO.getId());
                    question.setQuestionNumber(questionDTO.getQuestionNumber());
                    question.setQuestionText(questionDTO.getQuestionText());
                    question.setCorrectAnswer(questionDTO.getCorrectAnswer());
                    question.setExplanation(questionDTO.getExplanation());
                    question.setMarks(questionDTO.getMarks());
                    
                    // Set bidirectional relationship
                    question.setExam(destination);
                    
                    // Map options
                    if (questionDTO.getOptions() != null) {
                        for (ExamRequestDTO.OptionDTO optionDTO : questionDTO.getOptions()) {
                            Option option = new Option();
                            option.setId(optionDTO.getId());
                            option.setLabel(optionDTO.getOptionKey());
                            option.setText(optionDTO.getOptionText());
                            option.setIsCorrect(optionDTO.getIsCorrect());
                            
                            // Set bidirectional relationship
                            option.setQuestion(question);
                            question.addOption(option);
                        }
                    }
                    
                    destination.addQuestion(question);
                }
            }
            
            return destination;
        };
        
        entityMapper.getModelMapper().createTypeMap(ExamRequestDTO.class, Exam.class)
                .setConverter(examConverter);
    }
}
