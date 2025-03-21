package com.pharmacyhub.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.domain.entity.Option;
import com.pharmacyhub.domain.entity.Question;
import com.pharmacyhub.dto.request.JsonExamUploadRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for processing JSON data and creating exams
 */
@Service
public class JsonExamUploadService
{
    private static final Logger logger = LoggerFactory.getLogger(JsonExamUploadService.class);

    private final ExamService examService;
    private final ObjectMapper objectMapper;

    @Autowired
    public JsonExamUploadService(ExamService examService, ObjectMapper objectMapper)
    {
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
    public Exam processJsonAndCreateExam(JsonExamUploadRequestDTO requestDTO)
    {
        try
        {
            // Parse JSON content
            List<Map<String, Object>> jsonData = objectMapper.readValue(
                    requestDTO.getJsonContent(),
                    new TypeReference<List<Map<String, Object>>>()
                    {
                    }
            );

            // Create exam entity
            Exam exam = new Exam();
            exam.setTitle(requestDTO.getTitle());
            exam.setDescription(requestDTO.getDescription());
            exam.setDuration(requestDTO.getDuration());
            
            // Set premium fields
            exam.setPremium(requestDTO.getIsPremium() != null ? requestDTO.getIsPremium() : false);
            exam.setPrice(requestDTO.getPrice() != null ? BigDecimal.valueOf(requestDTO.getPrice()) : BigDecimal.ZERO);
            exam.setCustomPrice(requestDTO.getIsCustomPrice() != null ? requestDTO.getIsCustomPrice() : false);

            // We'll calculate totalMarks based on questions (default 1 mark per question)
            int totalMarks = jsonData.size();
            exam.setTotalMarks(totalMarks);

            // Set passing marks if provided, otherwise use default (60% of total)
            if (requestDTO.getPassingMarks() != null)
            {
                exam.setPassingMarks(requestDTO.getPassingMarks());
            }
            else
            {
                exam.setPassingMarks((int) Math.ceil(totalMarks * 0.6));
            }

            // Set status
            exam.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : Exam.ExamStatus.DRAFT);

            // Ensure tags list is initialized for both exam and requestDTO
            if (exam.getTags() == null)
            {
                exam.setTags(new ArrayList<>());
            }
            if (requestDTO.getTags() == null)
            {
                requestDTO.setTags(new ArrayList<>());
            }

            // Process paper type and add as tag
            if (requestDTO.getPaperType() != null && !requestDTO.getPaperType().isEmpty())
            {
                logger.info("Processing paper type: {}", requestDTO.getPaperType());

                // Add paper type as a tag if not already present
                if (!requestDTO.getTags().contains(requestDTO.getPaperType()))
                {
                    requestDTO.getTags().add(requestDTO.getPaperType());
                    logger.info("Added paper type tag: {}", requestDTO.getPaperType());
                }

                // Add default difficulty if not specified in metadata
//                if (requestDTO.getMetadata() != null && !requestDTO.getMetadata().containsKey("difficulty"))
//                {
//                    if (requestDTO.getDifficulty() != null)
//                    {
//                        requestDTO.getMetadata().put("difficulty", requestDTO.getDifficulty());
//                    }
//                }
            }

            // Process metadata and add as tags
            if (requestDTO.getMetadata() != null && !requestDTO.getMetadata().isEmpty())
            {
                logger.info("Processing metadata: {}", requestDTO.getMetadata());

                // Special handling for paper-type specific metadata
//                processSpecificPaperTypeMetadata(requestDTO);

                // Convert metadata entries to tags in format "key:value"
                for (Map.Entry<String, Object> entry : requestDTO.getMetadata().entrySet())
                {
                    if (entry.getValue() != null && !entry.getValue().toString().isEmpty())
                    {
                        String tag = entry.getKey() + ":" + entry.getValue().toString();
                        if (!requestDTO.getTags().contains(tag))
                        {
                            requestDTO.getTags().add(tag);
                            logger.debug("Added metadata tag: {}", tag);
                        }
                    }
                }
            }
            else
            {
                logger.info("No metadata provided or metadata is empty");
            }

            // Set tags
            exam.setTags(requestDTO.getTags());
            logger.info("Final tags set on exam: {}", exam.getTags());

            // Process questions
            List<Question> questions = processQuestions(jsonData, exam);

            // Process specific fields based on paper type
            if (requestDTO.getPaperType() != null && !requestDTO.getPaperType().isEmpty() &&
                    requestDTO.getMetadata() != null)
            {
                processPaperTypeSpecificFields(exam, requestDTO.getPaperType(), requestDTO.getMetadata());
            }

            exam.setQuestions(questions);

            // Create exam
            return examService.createExam(exam);

        }
        catch (Exception e)
        {
            logger.error("Error processing JSON data: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing JSON data: " + e.getMessage(), e);
        }
    }

    /**
     * Process JSON data and convert to Question entities
     *
     * @param jsonData The JSON data
     * @param exam     The parent exam
     * @return List of Question entities
     */
    private List<Question> processQuestions(List<Map<String, Object>> jsonData, Exam exam)
    {
        List<Question> questions = new ArrayList<>();

        for (int i = 0; i < jsonData.size(); i++)
        {
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
            if (questionData.containsKey("explanation"))
            {
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
    private int getQuestionNumber(Map<String, Object> questionData, int defaultIndex)
    {
        // Check different possible field names
        if (questionData.containsKey("number"))
        {
            return parseIntValue(questionData.get("number"));
        }
        else if (questionData.containsKey("question_number"))
        {
            return parseIntValue(questionData.get("question_number"));
        }
        else if (questionData.containsKey("questionNumber"))
        {
            return parseIntValue(questionData.get("questionNumber"));
        }

        // Default to index + 1
        return defaultIndex + 1;
    }

    /**
     * Get question text from data
     */
    private String getQuestionText(Map<String, Object> questionData)
    {
        // Check different possible field names
        if (questionData.containsKey("question"))
        {
            return questionData.get("question").toString();
        }
        else if (questionData.containsKey("text"))
        {
            return questionData.get("text").toString();
        }
        else if (questionData.containsKey("questionText"))
        {
            return questionData.get("questionText").toString();
        }

        throw new IllegalArgumentException("Question text not found in JSON data");
    }

    /**
     * Get correct answer from data
     */
    private String getCorrectAnswer(Map<String, Object> questionData)
    {
        if (!questionData.containsKey("answer"))
        {
            throw new IllegalArgumentException("Correct answer not found in JSON data");
        }

        String answer = questionData.get("answer").toString().trim();
        logger.debug("Original answer format: {}", answer);

        // Handle various answer formats
        if (answer.length() > 1)
        {
            // Format: "A)" or "A) Option text"
            if (answer.matches("^[A-Za-z]\\).*"))
            {
                answer = answer.substring(0, 1);
            }
            // Format: "A Option text"
            else if (answer.matches("^[A-Za-z]\\s.*"))
            {
                answer = answer.substring(0, 1);
            }
            // Format: "(A) Option text" or "(A) Option (with parentheses)"
            else if (answer.startsWith("(") && answer.length() >= 3)
            {
                answer = answer.substring(1, 2);
            }
            // Format: "Option A: text"
            else if (answer.contains("Option ") && answer.length() >= 8)
            {
                int optionIndex = answer.indexOf("Option ") + 7;
                if (optionIndex < answer.length())
                {
                    answer = String.valueOf(answer.charAt(optionIndex));
                }
            }
            // Format: "A. Option text"
            else if (answer.matches("^[A-Za-z]\\..*"))
            {
                answer = answer.substring(0, 1);
            }
            // Format: "Answer A" or "Answer: A"
            else if ((answer.startsWith("Answer ") || answer.startsWith("Answer: ")) && answer.length() >= 8)
            {
                answer = answer.substring(answer.lastIndexOf(" ") + 1);
                // If there's punctuation at the end, remove it
                if (answer.endsWith(".") || answer.endsWith(":") || answer.endsWith(","))
                {
                    answer = answer.substring(0, answer.length() - 1);
                }
            }
            // If all else fails, just use the first character if it's a letter
            else if (Character.isLetter(answer.charAt(0)))
            {
                answer = String.valueOf(answer.charAt(0));
            }
        }

        logger.debug("Extracted answer: {}", answer);
        return answer.toUpperCase();
    }

    /**
     * Process options from JSON data
     */
    private List<Option> processOptions(Object optionsObj, Question question)
    {
        List<Option> optionList = new ArrayList<>();

        if (optionsObj == null)
        {
            throw new IllegalArgumentException("Options not found in JSON data");
        }

        try
        {
            // If options is a Map with A, B, C, D keys
            if (optionsObj instanceof Map<?, ?>)
            {
                @SuppressWarnings("unchecked")
                Map<String, Object> optionsMap = (Map<String, Object>) optionsObj;

                for (Map.Entry<String, Object> entry : optionsMap.entrySet())
                {
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
            else if (optionsObj instanceof List<?>)
            {
                @SuppressWarnings("unchecked")
                List<Object> optionsList = (List<Object>) optionsObj;

                // Generate labels A, B, C, D...
                for (int i = 0; i < optionsList.size(); i++)
                {
                    // Generate standard labels (A, B, C, D...) for options
                    String label;
                    if (i < 26)
                    {
                        label = String.valueOf((char) ('A' + i));
                    }
                    else
                    {
                        // For more than 26 options, use AA, AB, etc.
                        label = String.valueOf((char) ('A' + (i / 26) - 1)) +
                                String.valueOf((char) ('A' + (i % 26)));
                    }

                    // Ensure label isn't longer than database can handle
                    if (label.length() > 10)
                    {
                        logger.warn("Option label '{}' is too long, truncating to 10 characters", label);
                        label = label.substring(0, 10);
                    }
                    String text;

                    // Handle different formats (string or object with text property)
                    if (optionsList.get(i) instanceof String)
                    {
                        text = (String) optionsList.get(i);
                    }
                    else if (optionsList.get(i) instanceof Map<?, ?>)
                    {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> optionMap = (Map<String, Object>) optionsList.get(i);
                        text = optionMap.get("text").toString();
                    }
                    else
                    {
                        throw new IllegalArgumentException("Invalid option format in JSON data");
                    }

                    Option option = new Option();
                    option.setQuestion(question);
                    option.setLabel(label);
                    option.setText(text);

                    optionList.add(option);
                }
            }
            else
            {
                throw new IllegalArgumentException("Invalid options format in JSON data");
            }

            // Mark the correct option
            String correctAnswer = question.getCorrectAnswer();
            for (Option option : optionList)
            {
                if (option.getLabel().equalsIgnoreCase(correctAnswer))
                {
                    option.setIsCorrect(true);
                }
                else
                {
                    option.setIsCorrect(false);
                }
            }

            return optionList;

        }
        catch (Exception e)
        {
            logger.error("Error processing options: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing options: " + e.getMessage(), e);
        }
    }

    /**
     * Parse integer value from object
     */
    private int parseIntValue(Object value)
    {
        if (value instanceof Integer)
        {
            return (Integer) value;
        }
        else if (value instanceof String)
        {
            return Integer.parseInt((String) value);
        }
        else if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }

        throw new IllegalArgumentException("Cannot parse integer value: " + value);
    }

    /**
     * Process paper type specific fields
     *
     * @param exam      The exam entity
     * @param paperType The paper type (MODEL, PAST, SUBJECT, PRACTICE)
     * @param metadata  The metadata map
     */
    private void processPaperTypeSpecificFields(Exam exam, String paperType, Map<String, Object> metadata)
    {
        logger.info("Processing specific fields for paper type: {}", paperType);

        switch (paperType.toUpperCase())
        {
            case "MODEL":
                processModelPaperFields(exam, metadata);
                break;
            case "PAST":
                processPastPaperFields(exam, metadata);
                break;
            case "SUBJECT":
                processSubjectPaperFields(exam, metadata);
                break;
            case "PRACTICE":
                processPracticePaperFields(exam, metadata);
                break;
            default:
                logger.warn("Unknown paper type: {}", paperType);
        }
    }

    /**
     * Process model paper fields
     *
     * @param exam     The exam entity
     * @param metadata The metadata map
     */
    private void processModelPaperFields(Exam exam, Map<String, Object> metadata)
    {
        // Set specific model paper fields as exam properties
        // These fields come from paperTypeUtils.getPaperTypeFields for MODEL
        if (metadata.containsKey("creator"))
        {
            logger.info("Setting creator from metadata: {}", metadata.get("creator"));
            // You could add additional fields to the Exam entity if needed
        }

        if (metadata.containsKey("targetAudience"))
        {
            logger.info("Setting targetAudience from metadata: {}", metadata.get("targetAudience"));
        }

        // Additional model paper fields: version, seriesName, recommendedPreparation
    }

    /**
     * Process past paper fields
     *
     * @param exam     The exam entity
     * @param metadata The metadata map
     */
    private void processPastPaperFields(Exam exam, Map<String, Object> metadata)
    {
        // Set specific past paper fields as exam properties
        // These fields come from paperTypeUtils.getPaperTypeFields for PAST
        if (metadata.containsKey("year"))
        {
            logger.info("Setting year from metadata: {}", metadata.get("year"));
        }

        if (metadata.containsKey("institution"))
        {
            logger.info("Setting institution from metadata: {}", metadata.get("institution"));
        }

        // Additional past paper fields: month, examBody, paperNumber, season, countryOrRegion
    }

    /**
     * Process subject paper fields
     *
     * @param exam     The exam entity
     * @param metadata The metadata map
     */
    private void processSubjectPaperFields(Exam exam, Map<String, Object> metadata)
    {
        // Set specific subject paper fields as exam properties
        // These fields come from paperTypeUtils.getPaperTypeFields for SUBJECT
        if (metadata.containsKey("subject"))
        {
            logger.info("Setting subject from metadata: {}", metadata.get("subject"));
        }

        if (metadata.containsKey("topic"))
        {
            logger.info("Setting topic from metadata: {}", metadata.get("topic"));
        }

        // Additional subject paper fields: subtopic, courseCode, curriculum, academicLevel
    }

    /**
     * Process practice paper fields
     *
     * @param exam     The exam entity
     * @param metadata The metadata map
     */
    private void processPracticePaperFields(Exam exam, Map<String, Object> metadata)
    {
        // Set specific practice paper fields as exam properties
        // These fields come from paperTypeUtils.getPaperTypeFields for PRACTICE
        if (metadata.containsKey("focusArea"))
        {
            logger.info("Setting focusArea from metadata: {}", metadata.get("focusArea"));
        }

        if (metadata.containsKey("skillLevel"))
        {
            logger.info("Setting skillLevel from metadata: {}", metadata.get("skillLevel"));
        }

        // Additional practice paper fields: learningObjectives, prerequisites, estimatedCompletionTime
    }

    /**
     * Extract and process specific metadata based on paper type
     */
    private void processPaperTypeSpecificMetadata(Exam exam, JsonExamUploadRequestDTO requestDTO)
    {
        if (requestDTO.getPaperType() == null || requestDTO.getMetadata() == null)
        {
            return;
        }

        String paperType = requestDTO.getPaperType();
        Map<String, Object> metadata = requestDTO.getMetadata();

        // Log the paper type and metadata for debugging
        logger.info("Processing paper type: {} with metadata: {}", paperType, metadata);

        switch (paperType)
        {
            case "PRACTICE":
                if (metadata.containsKey("focusArea"))
                {
                    exam.addTag("focusArea:" + metadata.get("focusArea"));
                }
                if (metadata.containsKey("skillLevel"))
                {
                    exam.addTag("skillLevel:" + metadata.get("skillLevel"));
                }
                // Additional fields for practice papers
                if (metadata.containsKey("learningObjectives"))
                {
                    exam.addTag("learningObjectives:" + metadata.get("learningObjectives"));
                }
                if (metadata.containsKey("prerequisites"))
                {
                    exam.addTag("prerequisites:" + metadata.get("prerequisites"));
                }
                if (metadata.containsKey("estimatedCompletionTime"))
                {
                    exam.addTag("estimatedCompletionTime:" + metadata.get("estimatedCompletionTime"));
                }
                break;
            case "PAST":
                if (metadata.containsKey("year"))
                {
                    exam.addTag("year:" + metadata.get("year"));
                }
                if (metadata.containsKey("month"))
                {
                    exam.addTag("month:" + metadata.get("month"));
                }
                // Additional fields for past papers
                if (metadata.containsKey("institution"))
                {
                    exam.addTag("institution:" + metadata.get("institution"));
                }
                if (metadata.containsKey("examBody"))
                {
                    exam.addTag("examBody:" + metadata.get("examBody"));
                }
                if (metadata.containsKey("paperNumber"))
                {
                    exam.addTag("paperNumber:" + metadata.get("paperNumber"));
                }
                if (metadata.containsKey("season"))
                {
                    exam.addTag("season:" + metadata.get("season"));
                }
                if (metadata.containsKey("countryOrRegion"))
                {
                    exam.addTag("countryOrRegion:" + metadata.get("countryOrRegion"));
                }
                break;
            case "SUBJECT":
                if (metadata.containsKey("subject"))
                {
                    exam.addTag("subject:" + metadata.get("subject"));
                }
                if (metadata.containsKey("topic"))
                {
                    exam.addTag("topic:" + metadata.get("topic"));
                }
                // Additional fields for subject papers
                if (metadata.containsKey("subtopic"))
                {
                    exam.addTag("subtopic:" + metadata.get("subtopic"));
                }
                if (metadata.containsKey("courseCode"))
                {
                    exam.addTag("courseCode:" + metadata.get("courseCode"));
                }
                if (metadata.containsKey("curriculum"))
                {
                    exam.addTag("curriculum:" + metadata.get("curriculum"));
                }
                if (metadata.containsKey("academicLevel"))
                {
                    exam.addTag("academicLevel:" + metadata.get("academicLevel"));
                }
                break;
            case "MODEL":
                if (metadata.containsKey("creator"))
                {
                    exam.addTag("creator:" + metadata.get("creator"));
                }
                // Additional fields for model papers
                if (metadata.containsKey("targetAudience"))
                {
                    exam.addTag("targetAudience:" + metadata.get("targetAudience"));
                }
                if (metadata.containsKey("version"))
                {
                    exam.addTag("version:" + metadata.get("version"));
                }
                if (metadata.containsKey("seriesName"))
                {
                    exam.addTag("seriesName:" + metadata.get("seriesName"));
                }
                if (metadata.containsKey("recommendedPreparation"))
                {
                    exam.addTag("recommendedPreparation:" + metadata.get("recommendedPreparation"));
                }
                break;
            default:
                logger.warn("Unknown paper type: {}", paperType);
                break;
        }

        // Add difficulty tag if present
        if (metadata.containsKey("difficulty"))
        {
            exam.addTag(metadata.get("difficulty").toString());
        }
    }

//    /**
    //     * Extract and process specific metadata based on paper type
    //     */
    //    private void processPaperTypeSpecificMetadata(Exam exam, JsonExamUploadRequestDTO requestDTO)
    //    {
    //        if (requestDTO.getPaperType() == null || requestDTO.getMetadata() == null)
    //        {
    //            return;
    //        }
    //
    //        String paperType = requestDTO.getPaperType();
    //        Map<String, Object> metadata = requestDTO.getMetadata();
    //
    //        // Log the paper type and metadata for debugging
    //        logger.info("Processing paper type: {} with metadata: {}", paperType, metadata);
    //
    //        switch (paperType)
    //        {
    //            case "PRACTICE":
    //                if (metadata.containsKey("focusArea"))
    //                {
    //                    exam.addTag("focusArea:" + metadata.get("focusArea"));
    //                }
    //                if (metadata.containsKey("skillLevel"))
    //                {
    //                    exam.addTag("skillLevel:" + metadata.get("skillLevel"));
    //                }
    //                // Additional fields for practice papers
    //                if (metadata.containsKey("learningObjectives"))
    //                {
    //                    exam.addTag("learningObjectives:" + metadata.get("learningObjectives"));
    //                }
    //                if (metadata.containsKey("prerequisites"))
    //                {
    //                    exam.addTag("prerequisites:" + metadata.get("prerequisites"));
    //                }
    //                if (metadata.containsKey("estimatedCompletionTime"))
    //                {
    //                    exam.addTag("estimatedCompletionTime:" + metadata.get("estimatedCompletionTime"));
    //                }
    //                break;
    //            case "PAST":
    //                if (metadata.containsKey("year"))
    //                {
    //                    exam.addTag("year:" + metadata.get("year"));
    //                }
    //                if (metadata.containsKey("month"))
    //                {
    //                    exam.addTag("month:" + metadata.get("month"));
    //                }
    //                // Additional fields for past papers
    //                if (metadata.containsKey("institution"))
    //                {
    //                    exam.addTag("institution:" + metadata.get("institution"));
    //                }
    //                if (metadata.containsKey("examBody"))
    //                {
    //                    exam.addTag("examBody:" + metadata.get("examBody"));
    //                }
    //                if (metadata.containsKey("paperNumber"))
    //                {
    //                    exam.addTag("paperNumber:" + metadata.get("paperNumber"));
    //                }
    //                if (metadata.containsKey("season"))
    //                {
    //                    exam.addTag("season:" + metadata.get("season"));
    //                }
    //                if (metadata.containsKey("countryOrRegion"))
    //                {
    //                    exam.addTag("countryOrRegion:" + metadata.get("countryOrRegion"));
    //                }
    //                break;
    //            case "SUBJECT":
    //                if (metadata.containsKey("subject"))
    //                {
    //                    exam.addTag("subject:" + metadata.get("subject"));
    //                }
    //                if (metadata.containsKey("topic"))
    //                {
    //                    exam.addTag("topic:" + metadata.get("topic"));
    //                }
    //                // Additional fields for subject papers
    //                if (metadata.containsKey("subtopic"))
    //                {
    //                    exam.addTag("subtopic:" + metadata.get("subtopic"));
    //                }
    //                if (metadata.containsKey("courseCode"))
    //                {
    //                    exam.addTag("courseCode:" + metadata.get("courseCode"));
    //                }
    //                if (metadata.containsKey("curriculum"))
    //                {
    //                    exam.addTag("curriculum:" + metadata.get("curriculum"));
    //                }
    //                if (metadata.containsKey("academicLevel"))
    //                {
    //                    exam.addTag("academicLevel:" + metadata.get("academicLevel"));
    //                }
    //                break;
    //            case "MODEL":
    //                if (metadata.containsKey("creator"))
    //                {
    //                    exam.addTag("creator:" + metadata.get("creator"));
    //                }
    //                // Additional fields for model papers
    //                if (metadata.containsKey("targetAudience"))
    //                {
    //                    exam.addTag("targetAudience:" + metadata.get("targetAudience"));
    //                }
    //                if (metadata.containsKey("version"))
    //                {
    //                    exam.addTag("version:" + metadata.get("version"));
    //                }
    //                if (metadata.containsKey("seriesName"))
    //                {
    //                    exam.addTag("seriesName:" + metadata.get("seriesName"));
    //                }
    //                if (metadata.containsKey("recommendedPreparation"))
    //                {
    //                    exam.addTag("recommendedPreparation:" + metadata.get("recommendedPreparation"));
    //                }
    //                break;
    //            default:
    //                logger.warn("Unknown paper type: {}", paperType);
    //                break;
    //        }
    //
    //        // Add difficulty tag if present
    //        if (metadata.containsKey("difficulty"))
    //        {
    //            exam.addTag(metadata.get("difficulty").toString());
    //        }
    //    }
}
