ALTER TABLE users ADD COLUMN password_hash VARCHAR(100);

ALTER TABLE challenges ADD COLUMN created_by_id BIGINT;
ALTER TABLE challenges ADD COLUMN role_track VARCHAR(80);
ALTER TABLE challenges ADD COLUMN challenge_type VARCHAR(120);
ALTER TABLE challenges ADD COLUMN focus_goal VARCHAR(160);
ALTER TABLE challenges ADD COLUMN generation_provider VARCHAR(32);
ALTER TABLE challenges ADD CONSTRAINT fk_challenge_owner FOREIGN KEY (created_by_id) REFERENCES users(id);

ALTER TABLE submissions ADD COLUMN answer_hash VARCHAR(64);
ALTER TABLE submissions ADD COLUMN idempotency_key VARCHAR(100);
ALTER TABLE submissions ADD COLUMN status VARCHAR(32) DEFAULT 'COMPLETED';
ALTER TABLE submissions ADD COLUMN error_message VARCHAR(1000);
ALTER TABLE submissions ADD COLUMN processing_started_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE submissions ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;
UPDATE submissions SET answer_hash = REPEAT('0', 64), idempotency_key = CONCAT('legacy-', id),
    status = 'COMPLETED', completed_at = submitted_at;
ALTER TABLE submissions ALTER COLUMN answer_hash SET NOT NULL;
ALTER TABLE submissions ALTER COLUMN idempotency_key SET NOT NULL;
ALTER TABLE submissions ALTER COLUMN status SET NOT NULL;
ALTER TABLE submissions ADD CONSTRAINT uk_submission_user_idempotency UNIQUE (user_id, idempotency_key);

UPDATE challenges c SET created_by_id = (SELECT MIN(s.user_id) FROM submissions s WHERE s.challenge_id = c.id
    GROUP BY s.challenge_id HAVING COUNT(DISTINCT s.user_id) = 1)
WHERE (SELECT COUNT(DISTINCT s.user_id) FROM submissions s WHERE s.challenge_id = c.id) = 1;
UPDATE challenges SET generation_provider = 'legacy' WHERE generation_provider IS NULL;

ALTER TABLE evaluations ADD COLUMN provider VARCHAR(32);
ALTER TABLE evaluations ALTER COLUMN feedback CLOB;
ALTER TABLE evaluations ADD COLUMN strengths_json CLOB;
ALTER TABLE evaluations ADD COLUMN improvements_json CLOB;
ALTER TABLE evaluations ADD COLUMN example_outline CLOB;
ALTER TABLE evaluations ADD COLUMN rubric_weights_json CLOB;
UPDATE evaluations SET provider = 'legacy', strengths_json = '[]', improvements_json = '[]',
    example_outline = 'Legacy evaluation', rubric_weights_json = '{"REQUIREMENT_UNDERSTANDING":0.25,"LOGICAL_CLARITY":0.20,"TECHNICAL_FEASIBILITY":0.25,"EDGE_CASE_COVERAGE":0.15,"COMMUNICATION_STRUCTURE":0.15}';
ALTER TABLE evaluations ALTER COLUMN provider SET NOT NULL;
ALTER TABLE evaluations ALTER COLUMN strengths_json SET NOT NULL;
ALTER TABLE evaluations ALTER COLUMN improvements_json SET NOT NULL;
ALTER TABLE evaluations ALTER COLUMN example_outline SET NOT NULL;
ALTER TABLE evaluations ALTER COLUMN rubric_weights_json SET NOT NULL;

CREATE INDEX idx_challenges_created_by ON challenges(created_by_id, created_at);
CREATE INDEX idx_submissions_user_submitted ON submissions(user_id, submitted_at);
CREATE INDEX idx_submissions_challenge_user ON submissions(challenge_id, user_id, submitted_at);
CREATE INDEX idx_submissions_status_started ON submissions(status, submitted_at);
CREATE INDEX idx_submissions_user_status_challenge ON submissions(user_id, status, challenge_id);
CREATE INDEX idx_challenge_requirements_challenge ON challenge_requirements(challenge_id);
CREATE INDEX idx_challenge_constraints_challenge ON challenge_constraints(challenge_id);
CREATE INDEX idx_challenge_criteria_challenge ON challenge_acceptance_criteria(challenge_id);
