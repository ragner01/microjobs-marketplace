-- Create schemas for multi-tenancy
CREATE SCHEMA IF NOT EXISTS jobs;
CREATE SCHEMA IF NOT EXISTS escrow;
CREATE SCHEMA IF NOT EXISTS shared;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Jobs Schema Tables
CREATE TABLE IF NOT EXISTS jobs.jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    title VARCHAR(500) NOT NULL,
    description TEXT,
    budget_amount DECIMAL(19,2) NOT NULL,
    budget_currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    deadline TIMESTAMP,
    required_skills TEXT[],
    location VARCHAR(500),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    max_distance_km DOUBLE PRECISION,
    client_id UUID NOT NULL,
    assigned_worker_id UUID,
    assigned_at TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT chk_budget_amount CHECK (budget_amount > 0),
    CONSTRAINT chk_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT chk_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180)),
    CONSTRAINT chk_max_distance CHECK (max_distance_km IS NULL OR max_distance_km > 0)
);

CREATE TABLE IF NOT EXISTS jobs.job_bids (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    job_id UUID NOT NULL REFERENCES jobs.jobs(id) ON DELETE CASCADE,
    worker_id UUID NOT NULL,
    bid_amount DECIMAL(19,2) NOT NULL,
    bid_currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    proposal TEXT,
    estimated_completion_days INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_bid_amount CHECK (bid_amount > 0),
    CONSTRAINT chk_completion_days CHECK (estimated_completion_days IS NULL OR estimated_completion_days > 0)
);

-- Escrow Schema Tables
CREATE TABLE IF NOT EXISTS escrow.escrow_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    account_holder_id UUID NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    balance_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    balance_currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_balance_amount CHECK (balance_amount >= 0),
    CONSTRAINT chk_account_type CHECK (account_type IN ('CLIENT', 'WORKER', 'PLATFORM', 'ESCROW_HOLD')),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED'))
);

CREATE TABLE IF NOT EXISTS escrow.ledger_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    escrow_account_id UUID NOT NULL REFERENCES escrow.escrow_accounts(id) ON DELETE CASCADE,
    entry_type VARCHAR(20) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    description TEXT NOT NULL,
    transaction_id UUID NOT NULL,
    posted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reference_number VARCHAR(100),
    CONSTRAINT chk_entry_amount CHECK (amount > 0),
    CONSTRAINT chk_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT'))
);

CREATE TABLE IF NOT EXISTS escrow.escrow_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    job_id UUID NOT NULL,
    client_id UUID NOT NULL,
    worker_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    type VARCHAR(50) NOT NULL,
    description TEXT,
    initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failure_reason TEXT,
    CONSTRAINT chk_transaction_amount CHECK (amount > 0),
    CONSTRAINT chk_transaction_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_transaction_type CHECK (type IN ('JOB_PAYMENT', 'DISPUTE_REFUND', 'PLATFORM_FEE', 'PENALTY'))
);

CREATE TABLE IF NOT EXISTS escrow.transaction_steps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    transaction_id UUID NOT NULL REFERENCES escrow.escrow_transactions(id) ON DELETE CASCADE,
    step_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    details TEXT,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failure_reason TEXT,
    CONSTRAINT chk_step_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

-- Shared Schema Tables
CREATE TABLE IF NOT EXISTS shared.outbox_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data TEXT NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMP,
    published_at TIMESTAMP,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED')),
    CONSTRAINT chk_retry_count CHECK (retry_count >= 0),
    CONSTRAINT chk_max_retries CHECK (max_retries > 0)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_jobs_tenant_id ON jobs.jobs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_jobs_status ON jobs.jobs(status);
CREATE INDEX IF NOT EXISTS idx_jobs_client_id ON jobs.jobs(client_id);
CREATE INDEX IF NOT EXISTS idx_jobs_worker_id ON jobs.jobs(assigned_worker_id);
CREATE INDEX IF NOT EXISTS idx_jobs_location ON jobs.jobs USING GIST (ll_to_earth(latitude, longitude));

CREATE INDEX IF NOT EXISTS idx_job_bids_job_id ON jobs.job_bids(job_id);
CREATE INDEX IF NOT EXISTS idx_job_bids_worker_id ON jobs.job_bids(worker_id);
CREATE INDEX IF NOT EXISTS idx_job_bids_status ON jobs.job_bids(status);

CREATE INDEX IF NOT EXISTS idx_escrow_accounts_tenant_id ON escrow.escrow_accounts(tenant_id);
CREATE INDEX IF NOT EXISTS idx_escrow_accounts_holder_id ON escrow.escrow_accounts(account_holder_id);
CREATE INDEX IF NOT EXISTS idx_escrow_accounts_type ON escrow.escrow_accounts(account_type);

CREATE INDEX IF NOT EXISTS idx_ledger_entries_account_id ON escrow.ledger_entries(escrow_account_id);
CREATE INDEX IF NOT EXISTS idx_ledger_entries_transaction_id ON escrow.ledger_entries(transaction_id);
CREATE INDEX IF NOT EXISTS idx_ledger_entries_posted_at ON escrow.ledger_entries(posted_at);

CREATE INDEX IF NOT EXISTS idx_escrow_transactions_tenant_id ON escrow.escrow_transactions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_escrow_transactions_job_id ON escrow.escrow_transactions(job_id);
CREATE INDEX IF NOT EXISTS idx_escrow_transactions_client_id ON escrow.escrow_transactions(client_id);
CREATE INDEX IF NOT EXISTS idx_escrow_transactions_worker_id ON escrow.escrow_transactions(worker_id);
CREATE INDEX IF NOT EXISTS idx_escrow_transactions_status ON escrow.escrow_transactions(status);

CREATE INDEX IF NOT EXISTS idx_transaction_steps_transaction_id ON escrow.transaction_steps(transaction_id);
CREATE INDEX IF NOT EXISTS idx_transaction_steps_status ON escrow.transaction_steps(status);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status ON shared.outbox_events(status);
CREATE INDEX IF NOT EXISTS idx_outbox_events_next_retry ON shared.outbox_events(next_retry_at);
CREATE INDEX IF NOT EXISTS idx_outbox_events_aggregate ON shared.outbox_events(aggregate_id, aggregate_type);

-- Create triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_jobs_updated_at BEFORE UPDATE ON jobs.jobs FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_job_bids_updated_at BEFORE UPDATE ON jobs.job_bids FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_escrow_accounts_updated_at BEFORE UPDATE ON escrow.escrow_accounts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_ledger_entries_updated_at BEFORE UPDATE ON escrow.ledger_entries FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_escrow_transactions_updated_at BEFORE UPDATE ON escrow.escrow_transactions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_transaction_steps_updated_at BEFORE UPDATE ON escrow.transaction_steps FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
