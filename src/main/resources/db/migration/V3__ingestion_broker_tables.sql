CREATE TABLE ingestion_processed_events (
    event_id VARCHAR(128) PRIMARY KEY,
    routing_key VARCHAR(128),
    source VARCHAR(128),
    occurred_at TIMESTAMP WITH TIME ZONE,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE rm_feedback_events (
    feedback_id VARCHAR(128) PRIMARY KEY,
    event_id VARCHAR(128) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    event_occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    author_person_id VARCHAR(64) NOT NULL,
    target_person_id VARCHAR(64) NOT NULL,
    target_name VARCHAR(255) NOT NULL,
    department_id VARCHAR(64),
    team_id VARCHAR(64),
    category_id VARCHAR(64) NOT NULL,
    category_name VARCHAR(255) NOT NULL,
    subcategory_id VARCHAR(64) NOT NULL,
    subcategory_name VARCHAR(255) NOT NULL,
    rating INT NOT NULL,
    feedback_date DATE NOT NULL
);

CREATE INDEX idx_rm_feedback_events_scope ON rm_feedback_events (feedback_date, department_id, team_id, target_person_id);
CREATE INDEX idx_rm_feedback_events_author_target ON rm_feedback_events (author_person_id, target_person_id, team_id);
