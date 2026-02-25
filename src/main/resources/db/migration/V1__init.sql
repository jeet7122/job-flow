CREATE TABLE jobs (
  id UUID PRIMARY KEY,
  type VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  priority INT NOT NULL DEFAULT 0,

  idempotency_key VARCHAR(128) UNIQUE,
  attempt INT NOT NULL DEFAULT 0,
  max_attempts INT NOT NULL DEFAULT 5,

  run_at TIMESTAMPTZ,
  locked_by VARCHAR(128),
  locked_until TIMESTAMPTZ,

  last_error TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_jobs_status_run_at ON jobs(status, run_at);
CREATE INDEX idx_jobs_type_status ON jobs(type, status);