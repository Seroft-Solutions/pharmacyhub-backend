package com.pharmacyhub.service;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.domain.entity.Option;
import com.pharmacyhub.domain.entity.Question;
import com.pharmacyhub.dto.request.JsonExamUploadRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for processing JSON data and creating exams
 */
@Service
public class JsonExamUploadService {
    private static final Logger logger = LoggerFactory.getLogger(JsonExamUploadService.class);
    
    private final ExamService examService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public JsonExamUploadService(ExamService examService, ObjectMapper objectMapper) {
        this.examService = examService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Process JSON data and create an exam
     * 
     * @param requestDTO The request containing exam metadata and JSON content
     * @return The created exam
     */
    @Transactional
    public Exam processJsonAndCreateExam(JsonExamUploadRequestDTO requestDTO) {
        try {
            // Parse JSON content
            List<Map<String, Object>> jsonData = objectMapper.readValue(
                    requestDTO.getJsonContent(), 
                    new TypeReference<List<Map<String, Object>>>() {}
            );
            
            // Create exam entity
            Exam exam = new Exam();
            exam.setTitle(requestDTO.getTitle());
            exam.setDescription(requestDTO.getDescription());
            exam.setDuration(requestDTO.getDuration());
            
            // We'll calculate totalMarks based on questions (default 1 mark per question)
            int totalMarks = jsonData.size();
            exam.setTotalMarks(totalMarks);
            
            // Set passing marks if provided, otherwise use default (60% of total)
            if (requestDTO.getPassingMarks() != null) {
                exam.setPassingMarks(requestDTO.getPassingMarks());
            } else {
                exam.setPassingMarks((int) Math.ceil(totalMarks * 0.6));
            }
            
            // Set status
            exam.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : Exam.ExamStatus.DRAFT);
            
            // Set tags
            exam.setTags(requestDTO.getTags());
            
            // Process questions
            List<Question> questions = processQuestions(jsonData, exam);
            exam.setQuestions(questions);
            
            // Create exam
            return examService.createExam(exam);
            
        } catch (Exception e) {
            logger.error("Error processing JSON data: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing JSON data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process JSON data and convert to Question entities
     * 
     * @param jsonData The JSON data
     * @param exam The parent exam
     * @return List of Question entities
     */
    private List<Question> processQuestions(List<Map<String, Object>> jsonData, Exam exam) {
        List<Question> questions = new ArrayList<>();
        
        for (int i = 0; i < jsonData.size(); i++) {
            Map<String, Object> questionData = jsonData.get(i);
            
            // Create question
            Question question = new Question();
            question.setExam(exam);
            
            // Set question number (use existing or create sequential)
            int questionNumber = getQuestionNumber(questionData, i);
            question.setQuestionNumber(questionNumber);
            
            // Set question text
            String questionText = getQuestionText(questionData);
            question.setQuestionText(questionText);
            
            // Set explanation if available
            if (questionData.containsKey("explanation")) {
                question.setExplanation(questionData.get("explanation").toString());
            }
            
            // Set default marks (1 per question)
            question.setMarks(1);
            
            // Process options
            Object optionsObj = questionData.get("options");
            List<Option> options = processOptions(optionsObj, question);
            question.setOptions(options);
            
            // Set correct answer
            String correctAnswer = getCorrectAnswer(questionData);
            question.setCorrectAnswer(correctAnswer);
            
            questions.add(question);
        }
        
        return questions;
    }
    
    /**
     * Get question number from data
     */
    private int getQuestionNumber(Map<String, Object> questionData, int defaultIndex) {
        // Check different possible field names
        if (questionData.containsKey("number")) {
            return parseIntValue(questionData.get("number"));
        } else if (questionData.containsKey("question_number")) {
            return parseIntValue(questionData.get("question_number"));
        } else if (questionData.containsKey("questionNumber")) {
            return parseIntValue(questionData.get("questionNumber"));
        }
        
        // Default to index + 1
        return defaultIndex + 1;
    }
    
    /**
     * Get question text from data
     */
    private String getQuestionText(Map<String, Object> questionData) {
        // Check different possible field names
        if (questionData.containsKey("question")) {
            return questionData.get("question").toString();
        } else if (questionData.containsKey("text")) {
            return questionData.get("text").toString();
        } else if (questionData.containsKey("questionText")) {
            return questionData.get("questionText").toString();
        }
        
        throw new IllegalArgumentException("Question text not found in JSON data");
    }
    
    /**
     * Get correct answer from data
     */
    private String getCorrectAnswer(Map<String, Object> questionData) {
        if (!questionData.containsKey("answer")) {
            throw new IllegalArgumentException("Correct answer not found in JSON data");
        }
        
        String answer = questionData.get("answer").toString();
        
        // Extract just the letter if it's in a format like "A)" or "A) Option text"
        if (answer.length() > 1) {
            if (answer.matches("^[A-D]\\).*") || answer.matches("^[A-D]\\s.*")) {
                answer = answer.substring(0, 1);
            }
        }
        
        return answer.toUpperCase();
    }
    
    /**
     * Process options from JSON data
     */
    private List<Option> processOptions(Object optionsObj, Question question) {
        List<Option> optionList = new ArrayList<>();
        
        if (optionsObj == null) {
            throw new IllegalArgumentException("Options not found in JSON data");
        }
        
        try {
            // If options is a Map with A, B, C, D keys
            if (optionsObj instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> optionsMap = (Map<String, Object>) optionsObj;
                
                for (Map.Entry<String, Object> entry : optionsMap.entrySet()) {
                    String label = entry.getKey();
                    String text = entry.getValue().toString();
                    
                    Option option = new Option();
                    option.setQuestion(question);
                    option.setLabel(label);
                    option.setText(text);
                    
                    // Don't set isCorrect here - we'll set it later based on the correctAnswer
                    
                    optionList.add(option);
                }
            }
            // If options is a List
            else if (optionsObj instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Object> optionsList = (List<Object>) optionsObj;
                
                // Generate labels A, B, C, D...
                for (int i = 0; i < optionsList.size(); i++) {
                    String label = String.valueOf((char) ('A' + i));
                    String text;
                    
                    // Handle different formats (string or object with text property)
                    if (optionsList.get(i) instanceof String) {
                        text = (String) optionsList.get(i);
                    } else if (optionsList.get(i) instanceof Map<?, ?>) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> optionMap = (Map<String, Object>) optionsList.get(i);
                        text = optionMap.get("text").toString();
                    } else {
                        throw new IllegalArgumentException("Invalid option format in JSON data");
                    }
                    
                    Option option = new Option();
                    option.setQuestion(question);
                    option.setLabel(label);
                    option.setText(text);
                    
                    optionList.add(option);
                }
            } else {
                throw new IllegalArgumentException("Invalid options format in JSON data");
            }
            
            // Mark the correct option
            String correctAnswer = question.getCorrectAnswer();
            for (Option option : optionList) {
                if (option.getLabel().equalsIgnoreCase(correctAnswer)) {
                    option.setIsCorrect(true);
                } else {
                    option.setIsCorrect(false);
                }
            }
            
            return optionList;
            
        } catch (Exception e) {
            logger.error("Error processing options: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing options: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse integer value from object
     */
    private int parseIntValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        
        throw new IllegalArgumentException("Cannot parse integer value: " + value);
    }
}
