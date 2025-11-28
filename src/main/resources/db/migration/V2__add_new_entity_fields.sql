-- V2: Add new fields to existing tables

-- Teams: Add AWS and integration configuration
ALTER TABLE teams ADD COLUMN IF NOT EXISTS aws_resources JSONB DEFAULT '{}';
ALTER TABLE teams ADD COLUMN IF NOT EXISTS integration_config JSONB DEFAULT '{}';
ALTER TABLE teams ADD COLUMN IF NOT EXISTS jira_project_key VARCHAR(100);
ALTER TABLE teams ADD COLUMN IF NOT EXISTS gitlab_project_id VARCHAR(255);

-- Sprints: Add committed and completed points
ALTER TABLE sprints ADD COLUMN IF NOT EXISTS committed_points INTEGER;
ALTER TABLE sprints ADD COLUMN IF NOT EXISTS completed_points INTEGER;

-- Alerts: Add team reference and additional fields
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS team_id BIGINT REFERENCES teams(id);
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS title VARCHAR(500);
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS source VARCHAR(100);
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS metadata JSONB DEFAULT '{}';
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS resolved BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS resolution TEXT;

-- Incidents: Add team reference and tracking fields
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS team_id BIGINT REFERENCES teams(id);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS source VARCHAR(100);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS started_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS acknowledged_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS mttr_minutes BIGINT;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS timeline TEXT;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS root_cause TEXT;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS resolution TEXT;

-- Metrics: Add team reference and metadata
ALTER TABLE metrics ADD COLUMN IF NOT EXISTS team_id BIGINT REFERENCES teams(id);
ALTER TABLE metrics ADD COLUMN IF NOT EXISTS metadata JSONB DEFAULT '{}';

-- New indexes for new columns
CREATE INDEX IF NOT EXISTS idx_alerts_team_id ON alerts(team_id);
CREATE INDEX IF NOT EXISTS idx_alerts_resolved ON alerts(resolved);
CREATE INDEX IF NOT EXISTS idx_incidents_team_id ON incidents(team_id);
CREATE INDEX IF NOT EXISTS idx_incidents_started_at ON incidents(started_at);
CREATE INDEX IF NOT EXISTS idx_incidents_mttr ON incidents(mttr_minutes);
CREATE INDEX IF NOT EXISTS idx_metrics_team_id ON metrics(team_id);
CREATE INDEX IF NOT EXISTS idx_teams_jira_project ON teams(jira_project_key);
CREATE INDEX IF NOT EXISTS idx_teams_gitlab_project ON teams(gitlab_project_id);

-- Update existing resolved_at to set resolved flag
UPDATE alerts SET resolved = TRUE WHERE resolved_at IS NOT NULL;

-- Set started_at from created_at for existing incidents
UPDATE incidents SET started_at = created_at WHERE started_at IS NULL;
