# Exam Feature Integration Guide

This document provides a guide for integrating the backend and frontend exam features in PharmacyHub.

## Overview

The exam feature allows users to:
1. Browse model papers and past papers
2. Take exams
3. View results and progress

## Backend Components

The backend implementation includes:

### Entities
- `Exam.java` - Basic exam entity
- `Question.java` - Questions for exams
- `Option.java` - Options for questions
- `ExamPaper.java` - Model papers and past papers
- `ExamAttempt.java` - User attempts at exams
- `UserAnswer.java` - User answers to questions
- `ExamResult.java` - Results of completed exams

### Services
- `ExamService` - Manages exams
- `QuestionService` - Manages questions
- `OptionService` - Manages options
- `ExamPaperService` - Manages exam papers
- `ExamAttemptService` - Manages exam attempts

### Controllers
- `ExamController` - API for exams
- `ExamPaperController` - API for exam papers
- `ExamAttemptController` - API for exam attempts

## Frontend Components

The frontend implementation uses:

### API Services
- `examService.ts` - API client for exams
- `mcqService.ts` - API client for MCQs
- `examPaperService.ts` - API client for exam papers

### State Management
- `examStore.ts` - Zustand store for exams
- `mcqExamStore.ts` - Zustand store for MCQs
- `examPaperStore.ts` - Zustand store for exam papers

## API Endpoints

### Exam Papers
- `GET /api/exams/papers/model` - Get model papers
- `GET /api/exams/papers/past` - Get past papers
- `GET /api/exams/papers/:id` - Get paper by ID
- `GET /api/exams/papers/stats` - Get exam statistics

### Exams
- `GET /api/exams/published` - Get published exams
- `GET /api/exams/:id` - Get exam by ID

### Exam Attempts
- `POST /api/exams/:id/start` - Start an exam attempt
- `POST /api/exams/attempts/:id/submit` - Submit an exam attempt

## Integration Steps

### 1. Database Migration

Run the following SQL migration script to create the new tables:

```sql
-- Create exam_papers table
CREATE TABLE exam_papers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty VARCHAR(10) NOT NULL,
    question_count INT NOT NULL,
    duration_minutes INT NOT NULL,
    premium BOOLEAN DEFAULT FALSE,
    attempt_count INT DEFAULT 0,
    success_rate_percent DOUBLE DEFAULT 0.0,
    last_updated_date DATE DEFAULT CURRENT_DATE,
    type VARCHAR(10) NOT NULL,
    exam_id BIGINT,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (exam_id) REFERENCES exams(id)
);

-- Create exam_paper_tags table
CREATE TABLE exam_paper_tags (
    exam_paper_id BIGINT NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (exam_paper_id, tag),
    FOREIGN KEY (exam_paper_id) REFERENCES exam_papers(id)
);

-- Create exam_attempts table
CREATE TABLE exam_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    score DOUBLE,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (exam_id) REFERENCES exams(id)
);

-- Create user_answers table
CREATE TABLE user_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id VARCHAR(10),
    time_spent INT NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (attempt_id) REFERENCES exam_attempts(id),
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- Create exam_results table
CREATE TABLE exam_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    score DOUBLE NOT NULL,
    total_questions INT NOT NULL,
    correct_answers INT NOT NULL,
    incorrect_answers INT NOT NULL,
    unanswered INT NOT NULL,
    time_spent INT NOT NULL,
    is_passed BOOLEAN NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (attempt_id) REFERENCES exam_attempts(id)
);
```

### 2. Backend Implementation

1. **Add Entity Classes**
   - Copy the new entity classes (`ExamPaper.java`, `ExamAttempt.java`, `UserAnswer.java`, `ExamResult.java`) to the `com.pharmacyhub.domain.entity` package.

2. **Add Repository Interfaces**
   - Copy the new repository interfaces (`ExamPaperRepository.java`, `ExamAttemptRepository.java`, `UserAnswerRepository.java`, `ExamResultRepository.java`) to the `com.pharmacyhub.domain.repository` package.

3. **Add DTOs**
   - Copy the new DTO classes (`ExamPaperDTO.java`, `ExamAttemptDTO.java`, `UserAnswerDTO.java`, `ExamResultDTO.java`, `ExamStatsDTO.java`) to the `com.pharmacyhub.dto` package.

4. **Add Service Interfaces and Implementations**
   - Copy the service interfaces (`ExamPaperService.java`, `ExamAttemptService.java`) to the `com.pharmacyhub.service` package.
   - Copy the service implementations (`ExamPaperServiceImpl.java`, `ExamAttemptServiceImpl.java`) to the `com.pharmacyhub.service` package.

5. **Add Controllers**
   - Copy the controllers (`ExamPaperController.java`, `ExamAttemptController.java`) to the `com.pharmacyhub.controller` package.

6. **Update Existing ExamController**
   - Modify `ExamController.java` to include the new endpoints for starting and submitting exams.

### 3. Create Sample Data

Insert sample data for testing:

```sql
-- Insert sample exam papers (MODEL)
INSERT INTO exam_papers (title, description, difficulty, question_count, duration_minutes, premium, attempt_count, success_rate_percent, type, exam_id)
VALUES 
('Pharmacology Basics 2024', 'Comprehensive review of basic pharmacology principles', 'EASY', 50, 60, false, 1250, 78.5, 'MODEL', 1),
('Clinical Pharmacy Practice', 'Advanced clinical pharmacy scenarios and case studies', 'HARD', 75, 90, true, 850, 65.0, 'MODEL', 2),
('Pharmaceutical Calculations', 'Essential calculations for pharmacy practice', 'MEDIUM', 40, 60, false, 2000, 72.3, 'MODEL', 3),
('Pharmacy Law & Ethics', 'Latest updates on pharmacy regulations and ethical practices', 'MEDIUM', 60, 75, true, 1500, 70.0, 'MODEL', 4);

-- Insert sample exam papers (PAST)
INSERT INTO exam_papers (title, description, difficulty, question_count, duration_minutes, premium, attempt_count, success_rate_percent, type, exam_id)
VALUES 
('2023 Board Exam Paper 1', 'Official board examination from 2023', 'HARD', 100, 180, true, 3000, 68.0, 'PAST', 5),
('2023 Board Exam Paper 2', 'Second paper from 2023 board examination', 'HARD', 100, 180, true, 2800, 65.0, 'PAST', 6);

-- Insert sample tags for exam papers
INSERT INTO exam_paper_tags (exam_paper_id, tag)
VALUES 
(1, 'Basic Pharmacology'),
(1, 'Drug Classification'),
(1, 'Mechanisms of Action'),
(2, 'Patient Care'),
(2, 'Clinical Decision Making'),
(2, 'Therapeutic Management'),
(3, 'Dosage Calculations'),
(3, 'Concentration Calculations'),
(3, 'Compounding'),
(4, 'Pharmacy Laws'),
(4, 'Professional Ethics'),
(4, 'Regulatory Compliance'),
(5, 'Comprehensive'),
(5, 'Clinical Practice'),
(5, 'Pharmacy Management'),
(6, 'Drug Therapy'),
(6, 'Patient Care'),
(6, 'Pharmacy Operations');
```

### 4. Frontend Integration

1. **Update API Services**
   - Update `examService.ts` to use real API calls instead of mock data
   - Update `mcqService.ts` to use real API calls instead of mock data
   - Create `examPaperService.ts` to handle exam paper related API calls

2. **Update State Management**
   - Update existing Zustand stores to work with the new API endpoints
   - Create `examPaperStore.ts` for managing exam papers state

3. **Update UI Components**
   - Update UI components to use the new stores and APIs
   - Ensure error handling is in place for API calls
   - Add loading states for better user experience

### 5. Testing

1. **Backend Testing**
   - Test each API endpoint using Postman or similar tools
   - Verify data is correctly stored in the database
   - Check error handling for edge cases

2. **Frontend Testing**
   - Test the browse exams feature
   - Test the take exam flow from start to finish
   - Test viewing results and progress

### 6. Integration Testing

1. **Full Flow Testing**
   - Test browsing exam papers
   - Test starting an exam
   - Test submitting answers
   - Test viewing results

### 7. Deployment

1. **Database Migration**
   - Run migration scripts on production database
   - Verify tables are created correctly

2. **Backend Deployment**
   - Deploy backend changes
   - Verify API endpoints are accessible

3. **Frontend Deployment**
   - Deploy frontend changes
   - Verify integration is working in production

## Data Formats

### Frontend to Backend

When starting an exam:
```
POST /api/exams/{id}/start?userId={userId}
```

When submitting answers:
```json
[
  {
    "questionId": 1,
    "selectedOptionId": "A",
    "timeSpent": 45
  },
  {
    "questionId": 2,
    "selectedOptionId": "B",
    "timeSpent": 30
  }
]
```

### Backend to Frontend

Exam papers:
```json
[
  {
    "id": 1,
    "title": "Pharmacology Basics 2024",
    "description": "Comprehensive review...",
    "difficulty": "easy",
    "questionCount": 50,
    "durationMinutes": 60,
    "tags": ["Basic Pharmacology", "Drug Classification"],
    "premium": false,
    "attemptCount": 1250,
    "successRatePercent": 78.5,
    "lastUpdatedDate": "2024-02-15",
    "type": "MODEL"
  }
]
```

Exam stats:
```json
{
  "totalPapers": 10,
  "avgDuration": 45,
  "completionRate": 75,
  "activeUsers": 125
}
```

Exam result:
```json
{
  "examId": 1,
  "examTitle": "Basic Pharmacology Exam",
  "score": 75.0,
  "totalMarks": 100,
  "passingMarks": 60,
  "isPassed": true,
  "timeSpent": 320,
  "questionResults": [
    {
      "questionId": 1,
      "questionText": "Which of the following is NOT a phase of pharmacokinetics?",
      "userAnswerId": "A",
      "correctAnswerId": "E",
      "isCorrect": false,
      "explanation": "Receptor binding is part of pharmacodynamics...",
      "points": 10,
      "earnedPoints": 0
    }
  ]
}
```

## Troubleshooting

### Common Issues

1. **API Errors**
   - Check API endpoint URLs in frontend services
   - Verify authentication headers are being sent
   - Check backend logs for detailed error messages

2. **Database Issues**
   - Verify foreign key constraints
   - Check database connection settings
   - Ensure database migrations have been applied

3. **Frontend State Management**
   - Debug Zustand stores with browser dev tools
   - Verify actions are properly updating state
   - Check for race conditions in async operations

### Support Contacts

For backend issues, contact:
- Backend Team Lead: backendlead@pharmacyhub.com

For frontend issues, contact:
- Frontend Team Lead: frontendlead@pharmacyhub.com

For database issues, contact:
- Database Administrator: dba@pharmacyhub.com
