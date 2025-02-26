# Exam Feature API Documentation

This document describes the RESTful API endpoints available for the Exam feature in the PharmacyHub application.

## Table of Contents

- [Exam API](#exam-api)
- [Exam Paper API](#exam-paper-api)
- [Exam Attempt API](#exam-attempt-api)

## Exam API

### Get All Exams

```
GET /api/exams
```

Returns a list of all active exams.

### Get Published Exams

```
GET /api/exams/published
```

Returns a list of all published exams.

### Get Exam by ID

```
GET /api/exams/{id}
```

Returns a specific exam by its ID.

### Create Exam

```
POST /api/exams
```

Creates a new exam.

**Request Body:**

```json
{
  "title": "Basic Pharmacology Exam",
  "description": "Test your knowledge of basic pharmacology concepts",
  "duration": 30,
  "totalMarks": 100,
  "passingMarks": 60
}
```

### Update Exam

```
PUT /api/exams/{id}
```

Updates an existing exam.

**Request Body:**

Same as create exam.

### Delete Exam

```
DELETE /api/exams/{id}
```

Marks an exam as deleted (soft delete).

### Get Exams by Status

```
GET /api/exams/status/{status}
```

Returns exams filtered by status (DRAFT, PUBLISHED, ARCHIVED).

### Publish Exam

```
POST /api/exams/{id}/publish
```

Changes an exam's status to PUBLISHED.

### Archive Exam

```
POST /api/exams/{id}/archive
```

Changes an exam's status to ARCHIVED.

## Exam Paper API

### Get All Papers

```
GET /api/exams/papers
```

Returns a list of all active exam papers.

### Get Model Papers

```
GET /api/exams/papers/model
```

Returns a list of all model papers.

### Get Past Papers

```
GET /api/exams/papers/past
```

Returns a list of all past papers.

### Get Paper by ID

```
GET /api/exams/papers/{id}
```

Returns a specific exam paper by its ID.

### Get Exam Stats

```
GET /api/exams/papers/stats
```

Returns statistics about exams including total papers, average duration, completion rate, and active users.

**Response:**

```json
{
  "totalPapers": 10,
  "avgDuration": 45,
  "completionRate": 75,
  "activeUsers": 125
}
```

### Create Paper

```
POST /api/exams/papers
```

Creates a new exam paper.

**Request Body:**

```json
{
  "title": "Pharmacology Basics 2024",
  "description": "Comprehensive review of basic pharmacology principles",
  "difficulty": "easy",
  "questionCount": 50,
  "durationMinutes": 60,
  "tags": ["Basic Pharmacology", "Drug Classification", "Mechanisms of Action"],
  "premium": false,
  "type": "MODEL",
  "examId": 1
}
```

### Update Paper

```
PUT /api/exams/papers/{id}
```

Updates an existing exam paper.

**Request Body:**

Same as create paper.

### Delete Paper

```
DELETE /api/exams/papers/{id}
```

Marks a paper as deleted (soft delete).

## Exam Attempt API

### Start Exam

```
POST /api/exams/{id}/start?userId={userId}
```

Starts an exam attempt for the specified user.

**Response:**

```json
{
  "id": 1,
  "examId": 1,
  "userId": "user123",
  "startTime": "2024-02-26T10:30:00Z",
  "status": "IN_PROGRESS",
  "answers": []
}
```

### Submit Exam

```
POST /api/exams/attempts/{attemptId}/submit
```

Submits an exam attempt with user answers.

**Request Body:**

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

**Response:**

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
      "explanation": "Receptor binding is a part of pharmacodynamics, not pharmacokinetics.",
      "points": 10,
      "earnedPoints": 0
    },
    {
      "questionId": 2,
      "questionText": "Which of the following best describes first-pass metabolism?",
      "userAnswerId": "B",
      "correctAnswerId": "B",
      "isCorrect": true,
      "explanation": "First-pass metabolism refers to the metabolic degradation of a drug in the liver after absorption from the GI tract but before it reaches the systemic circulation.",
      "points": 10,
      "earnedPoints": 10
    }
  ]
}
```

### Get User's Attempts

```
GET /api/exams/attempts/user/{userId}
```

Returns all exam attempts for a specific user.

### Get Attempt by ID

```
GET /api/exams/attempts/{id}
```

Returns a specific exam attempt by its ID.

### Get Attempts by Exam and User

```
GET /api/exams/{examId}/attempts?userId={userId}
```

Returns all attempts for a specific exam by a specific user.

## Data Models

### Exam

```json
{
  "id": 1,
  "title": "Basic Pharmacology Exam",
  "description": "Test your knowledge of basic pharmacology concepts",
  "duration": 30,
  "totalMarks": 100,
  "passingMarks": 60,
  "status": "PUBLISHED",
  "questions": [...]
}
```

### Exam Paper

```json
{
  "id": 1,
  "title": "Pharmacology Basics 2024",
  "description": "Comprehensive review of basic pharmacology principles",
  "difficulty": "easy",
  "questionCount": 50,
  "durationMinutes": 60,
  "tags": ["Basic Pharmacology", "Drug Classification", "Mechanisms of Action"],
  "premium": false,
  "attemptCount": 1250,
  "successRatePercent": 78.5,
  "lastUpdatedDate": "2024-02-15",
  "type": "MODEL",
  "examId": 1
}
```

### Question

```json
{
  "id": 1,
  "questionNumber": 1,
  "questionText": "Which of the following is NOT a phase of pharmacokinetics?",
  "options": [...],
  "correctAnswer": "E",
  "explanation": "Receptor binding is a part of pharmacodynamics, not pharmacokinetics.",
  "marks": 10
}
```

### Exam Attempt

```json
{
  "id": 1,
  "examId": 1,
  "userId": "user123",
  "startTime": "2024-02-26T10:30:00Z",
  "status": "IN_PROGRESS",
  "answers": [...]
}
```

### User Answer

```json
{
  "questionId": 1,
  "selectedOptionId": "A",
  "timeSpent": 45
}
```

### Exam Result

```json
{
  "examId": 1,
  "examTitle": "Basic Pharmacology Exam",
  "score": 75.0,
  "totalMarks": 100,
  "passingMarks": 60,
  "isPassed": true,
  "timeSpent": 320,
  "questionResults": [...]
}
```

## Error Responses

The API returns appropriate HTTP status codes for different types of errors:

- `400 Bad Request` - Invalid input data
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server-side error

Error responses include a message in the following format:

```json
{
  "timestamp": "2024-02-26T12:34:56.789Z",
  "status": 404,
  "error": "Not Found",
  "message": "Exam not found with id: 123",
  "path": "/api/exams/123"
}
```
