package com.pharmacy.hub.features.exam.infrastructure.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.hub.features.exam.domain.entity.Exam;
import com.pharmacy.hub.features.exam.domain.entity.Option;
import com.pharmacy.hub.features.exam.domain.entity.Question;
import com.pharmacy.hub.features.exam.domain.repository.ExamRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExamDataLoader
{

    private final ExamRepository examRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.exam.data-location:classpath:data/exams/*.json}")
    private String examDataLocation;

    @PostConstruct
    @Transactional
    public void loadExamData()
    {
        try
        {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(examDataLocation);

            Arrays.stream(resources).forEach(this::loadExamFromJson);

            log.info("Successfully loaded {} exam files", resources.length);
        }
        catch (IOException e)
        {
            log.error("Error loading exam data files", e);
        }
    }

    private void loadExamFromJson(Resource resource)
    {
        try
        {
            Map<String, Object> examData = objectMapper.readValue(resource.getInputStream(), Map.class);

            Exam exam = new Exam();
            exam.setTitle((String) examData.get("title"));
            exam.setDescription((String) examData.get("description"));
            exam.setDuration((Integer) examData.get("duration"));
            exam.setTotalMarks((Integer) examData.get("totalMarks"));
            exam.setPassingMarks((Integer) examData.get("passingMarks"));
            exam.setStatus(Exam.ExamStatus.DRAFT);

            List<Map<String, Object>> questionsData = (List<Map<String, Object>>) examData.get("questions");
            for (Map<String, Object> questionData : questionsData)
            {
                Question question = createQuestion(questionData);
                exam.addQuestion(question);
            }

            if (!examRepository.existsByTitle(exam.getTitle()))
            {
                examRepository.save(exam);
                log.info("Loaded exam: {}", exam.getTitle());
            }
            else
            {
                log.info("Skipped existing exam: {}", exam.getTitle());
            }
        }
        catch (IOException e)
        {
            log.error("Error loading exam from {}", resource.getFilename(), e);
        }
    }

    private Question createQuestion(Map<String, Object> questionData)
    {
        Question question = new Question();
        question.setQuestionText((String) questionData.get("text"));
        question.setQuestionNumber((Integer) questionData.get("number"));
        question.setCorrectAnswer((String) questionData.get("correctAnswer"));
        question.setExplanation((String) questionData.get("explanation"));
        question.setMarks((Integer) questionData.getOrDefault("marks", 1));
        question.setType(Question.QuestionType.MCQ);

        Map<String, String> options = (Map<String, String>) questionData.get("options");
        options.forEach((label, text) -> {
            Option option = new Option();
            option.setLabel(label);
            option.setText(text);
            option.setIsCorrect(label.equals(question.getCorrectAnswer()));
            question.addOption(option);
        });

        return question;
    }
}
