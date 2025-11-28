-- Group Lead Initial Schema

-- Users table (for authentication)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Teams table
CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Developers table
CREATE TABLE developers (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT REFERENCES teams(id),
    user_id BIGINT REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    avatar_url VARCHAR(500),
    role VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Developer External IDs (for JIRA, GitLab, etc.)
CREATE TABLE developer_external_ids (
    developer_id BIGINT NOT NULL REFERENCES developers(id) ON DELETE CASCADE,
    source VARCHAR(50) NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (developer_id, source)
);

-- Sprints table
CREATE TABLE sprints (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT REFERENCES teams(id),
    external_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    goal TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(external_id)
);

-- Tickets table
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL,
    source VARCHAR(50) NOT NULL,
    developer_id BIGINT REFERENCES developers(id),
    sprint_id BIGINT REFERENCES sprints(id),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50),
    ticket_type VARCHAR(50),
    story_points INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    external_updated_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(external_id, source)
);

-- Ticket Labels
CREATE TABLE ticket_labels (
    ticket_id BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    label VARCHAR(100) NOT NULL,
    PRIMARY KEY (ticket_id, label)
);

-- Commits table
CREATE TABLE commits (
    id BIGSERIAL PRIMARY KEY,
    developer_id BIGINT REFERENCES developers(id),
    sha VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    additions INTEGER NOT NULL DEFAULT 0,
    deletions INTEGER NOT NULL DEFAULT 0,
    files_changed INTEGER NOT NULL DEFAULT 0,
    project_id VARCHAR(255),
    branch VARCHAR(255),
    committed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(sha)
);

-- Merge Requests table
CREATE TABLE merge_requests (
    id BIGSERIAL PRIMARY KEY,
    developer_id BIGINT REFERENCES developers(id),
    external_id VARCHAR(255) NOT NULL,
    project_id VARCHAR(255),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    source_branch VARCHAR(255),
    target_branch VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    additions INTEGER NOT NULL DEFAULT 0,
    deletions INTEGER NOT NULL DEFAULT 0,
    comments_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    merged_at TIMESTAMP WITH TIME ZONE,
    deployed_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(external_id, project_id)
);

-- Deployments table
CREATE TABLE deployments (
    id BIGSERIAL PRIMARY KEY,
    merge_request_id BIGINT REFERENCES merge_requests(id),
    environment VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    version VARCHAR(100),
    caused_incident BOOLEAN DEFAULT FALSE,
    deployed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Incidents table
CREATE TABLE incidents (
    id BIGSERIAL PRIMARY KEY,
    deployment_id BIGINT REFERENCES deployments(id),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    severity VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Metrics table
CREATE TABLE metrics (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(50),
    source VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Metric Tags
CREATE TABLE metric_tags (
    metric_id BIGINT NOT NULL REFERENCES metrics(id) ON DELETE CASCADE,
    tag_key VARCHAR(100) NOT NULL,
    tag_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (metric_id, tag_key)
);

-- Alerts table
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    metric_name VARCHAR(255),
    metric_value DOUBLE PRECISION,
    threshold_value DOUBLE PRECISION,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_by BIGINT REFERENCES users(id),
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- AI Insights table
CREATE TABLE ai_insights (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    target_id VARCHAR(255) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    structured_content JSONB,
    confidence_score DOUBLE PRECISION,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Chat History table
CREATE TABLE chat_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    session_id VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    related_metrics JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_tickets_developer_id ON tickets(developer_id);
CREATE INDEX idx_tickets_sprint_id ON tickets(sprint_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_created_at ON tickets(created_at);
CREATE INDEX idx_tickets_source ON tickets(source);

CREATE INDEX idx_commits_developer_id ON commits(developer_id);
CREATE INDEX idx_commits_committed_at ON commits(committed_at);

CREATE INDEX idx_merge_requests_developer_id ON merge_requests(developer_id);
CREATE INDEX idx_merge_requests_status ON merge_requests(status);
CREATE INDEX idx_merge_requests_created_at ON merge_requests(created_at);

CREATE INDEX idx_metrics_type_timestamp ON metrics(type, timestamp);
CREATE INDEX idx_metrics_source ON metrics(source);
CREATE INDEX idx_metrics_timestamp ON metrics(timestamp);

CREATE INDEX idx_alerts_type ON alerts(type);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_created_at ON alerts(created_at);
CREATE INDEX idx_alerts_resolved_at ON alerts(resolved_at);

CREATE INDEX idx_ai_insights_type ON ai_insights(type);
CREATE INDEX idx_ai_insights_target ON ai_insights(target_id, target_type);
CREATE INDEX idx_ai_insights_generated_at ON ai_insights(generated_at);

CREATE INDEX idx_sprints_team_id ON sprints(team_id);
CREATE INDEX idx_sprints_status ON sprints(status);
CREATE INDEX idx_sprints_dates ON sprints(start_date, end_date);

CREATE INDEX idx_developers_team_id ON developers(team_id);
CREATE INDEX idx_developers_email ON developers(email);

CREATE INDEX idx_chat_history_user_session ON chat_history(user_id, session_id);
CREATE INDEX idx_chat_history_created_at ON chat_history(created_at);

-- Deployments indexes
CREATE INDEX idx_deployments_environment ON deployments(environment);
CREATE INDEX idx_deployments_deployed_at ON deployments(deployed_at);

-- Incidents indexes
CREATE INDEX idx_incidents_severity ON incidents(severity);
CREATE INDEX idx_incidents_status ON incidents(status);
CREATE INDEX idx_incidents_created_at ON incidents(created_at);
