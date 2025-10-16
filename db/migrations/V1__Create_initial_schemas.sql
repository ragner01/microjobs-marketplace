-- Create schemas for multi-tenancy
CREATE SCHEMA IF NOT EXISTS shared;
CREATE SCHEMA IF NOT EXISTS jobs;
CREATE SCHEMA IF NOT EXISTS escrow;

-- Create shared outbox table
CREATE TABLE IF NOT EXISTS shared.outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_data TEXT NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 5,
    next_retry_at TIMESTAMP,
    published_at TIMESTAMP,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create jobs schema tables
CREATE TABLE IF NOT EXISTS jobs.jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    title VARCHAR(255) NOT NULL,
    description TEXT,
    budget_amount DECIMAL(19,2) NOT NULL,
    budget_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    deadline TIMESTAMP,
    location VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    max_distance_km DOUBLE PRECISION,
    client_id UUID NOT NULL,
    assigned_worker_id UUID,
    assigned_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS jobs.job_bids (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    job_id UUID NOT NULL REFERENCES jobs.jobs(id),
    worker_id UUID NOT NULL,
    bid_amount DECIMAL(19,2) NOT NULL,
    bid_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    proposal TEXT NOT NULL,
    estimated_completion_days INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create escrow schema tables
CREATE TABLE IF NOT EXISTS escrow.escrow_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    account_holder_id UUID NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    balance_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    balance_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS escrow.ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    escrow_account_id UUID NOT NULL REFERENCES escrow.escrow_accounts(id),
    entry_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    description TEXT NOT NULL,
    transaction_id UUID NOT NULL,
    entry_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS escrow.escrow_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    job_id UUID NOT NULL,
    client_id UUID NOT NULL,
    worker_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    description TEXT,
    initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS escrow.transaction_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    transaction_id UUID NOT NULL REFERENCES escrow.escrow_transactions(id),
    step_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    executed_at TIMESTAMP,
    failure_reason TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_outbox_events_status ON shared.outbox_events(status);
CREATE INDEX IF NOT EXISTS idx_outbox_events_tenant ON shared.outbox_events(tenant_id);
CREATE INDEX IF NOT EXISTS idx_outbox_events_retry ON shared.outbox_events(next_retry_at);

CREATE INDEX IF NOT EXISTS idx_jobs_tenant ON jobs.jobs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_jobs_status ON jobs.jobs(status);
CREATE INDEX IF NOT EXISTS idx_jobs_client ON jobs.jobs(client_id);
CREATE INDEX IF NOT EXISTS idx_jobs_worker ON jobs.jobs(assigned_worker_id);

CREATE INDEX IF NOT EXISTS idx_job_bids_job ON jobs.job_bids(job_id);
CREATE INDEX IF NOT EXISTS idx_job_bids_worker ON jobs.job_bids(worker_id);
CREATE INDEX IF NOT EXISTS idx_job_bids_status ON jobs.job_bids(status);

CREATE INDEX IF NOT EXISTS idx_escrow_accounts_holder ON escrow.escrow_accounts(account_holder_id);
CREATE INDEX IF NOT EXISTS idx_escrow_accounts_type ON escrow.escrow_accounts(account_type);
CREATE INDEX IF NOT EXISTS idx_escrow_accounts_tenant ON escrow.escrow_accounts(tenant_id);

CREATE INDEX IF NOT EXISTS idx_ledger_entries_account ON escrow.ledger_entries(escrow_account_id);
CREATE INDEX IF NOT EXISTS idx_ledger_entries_transaction ON escrow.ledger_entries(transaction_id);

CREATE INDEX IF NOT EXISTS idx_escrow_transactions_job ON escrow.escrow_transactions(job_id);
CREATE INDEX IF NOT EXISTS idx_escrow_transactions_client ON escrow.escrow_transactions(client_id);
CREATE INDEX IF NOT EXISTS idx_escrow_transactions_worker ON escrow.escrow_transactions(worker_id);
CREATE INDEX IF NOT EXISTS idx_escrow_transactions_status ON escrow.escrow_transactions(status);

CREATE INDEX IF NOT EXISTS idx_transaction_steps_transaction ON escrow.transaction_steps(transaction_id);
CREATE INDEX IF NOT EXISTS idx_transaction_steps_status ON escrow.transaction_steps(status);
