-- Create flagged_questions table
CREATE TABLE IF NOT EXISTS flagged_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (attempt_id) REFERENCES exam_attempts(id),
    FOREIGN KEY (question_id) REFERENCES questions(id),
    UNIQUE (attempt_id, question_id)
);

-- Create index for faster lookup
CREATE INDEX IF NOT EXISTS idx_flagged_questions_attempt ON flagged_questions(attempt_id);
CREATE INDEX IF NOT EXISTS idx_flagged_questions_question ON flagged_questions(question_id);
