-- Create schemas for multi-tenancy
CREATE SCHEMA IF NOT EXISTS shared;
CREATE SCHEMA IF NOT EXISTS jobs;
CREATE SCHEMA IF NOT EXISTS escrow;
CREATE SCHEMA IF NOT EXISTS notifications;
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS analytics;

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

-- notifications schema
CREATE SCHEMA IF NOT EXISTS notifications AUTHORIZATION microjobs;

CREATE TABLE IF NOT EXISTS notifications.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    recipient_id UUID NOT NULL,
    sender_id UUID,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'UNREAD',
    read_at TIMESTAMP,
    metadata TEXT,
    priority INTEGER NOT NULL DEFAULT 1,
    expires_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications.notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL UNIQUE,
    title_template VARCHAR(255) NOT NULL,
    message_template TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    default_priority INTEGER NOT NULL DEFAULT 1,
    description TEXT
);

-- Create indexes for notifications
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications.notifications(recipient_id);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications.notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications.notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_tenant ON notifications.notifications(tenant_id);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications.notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_priority ON notifications.notifications(priority);

CREATE INDEX IF NOT EXISTS idx_notification_templates_type ON notifications.notification_templates(type);
CREATE INDEX IF NOT EXISTS idx_notification_templates_active ON notifications.notification_templates(is_active);

-- analytics schema
CREATE SCHEMA IF NOT EXISTS analytics AUTHORIZATION microjobs;

CREATE TABLE IF NOT EXISTS analytics.job_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    job_id UUID NOT NULL,
    client_id UUID NOT NULL,
    worker_id UUID,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    budget_amount DECIMAL(15,2) NOT NULL,
    budget_currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at_job TIMESTAMP NOT NULL,
    assigned_at TIMESTAMP,
    completed_at TIMESTAMP,
    bid_count INTEGER NOT NULL DEFAULT 0,
    average_bid_amount DECIMAL(15,2),
    completion_time_hours BIGINT,
    client_rating DECIMAL(3,2),
    worker_rating DECIMAL(3,2),
    is_urgent BOOLEAN NOT NULL DEFAULT FALSE,
    is_remote BOOLEAN NOT NULL DEFAULT FALSE,
    location VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS analytics.user_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    user_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at_user TIMESTAMP NOT NULL,
    last_active_at TIMESTAMP,
    total_jobs_posted INTEGER NOT NULL DEFAULT 0,
    total_jobs_completed INTEGER NOT NULL DEFAULT 0,
    total_jobs_assigned INTEGER NOT NULL DEFAULT 0,
    total_earnings DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_spent DECIMAL(15,2) NOT NULL DEFAULT 0,
    average_rating DECIMAL(3,2) NOT NULL DEFAULT 0,
    total_ratings INTEGER NOT NULL DEFAULT 0,
    completion_rate DECIMAL(5,4) NOT NULL DEFAULT 0,
    response_time_hours DECIMAL(8,2) NOT NULL DEFAULT 0,
    location VARCHAR(255),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_premium BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS analytics.platform_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    metric_date TIMESTAMP NOT NULL,
    total_users INTEGER NOT NULL DEFAULT 0,
    active_users INTEGER NOT NULL DEFAULT 0,
    total_jobs INTEGER NOT NULL DEFAULT 0,
    active_jobs INTEGER NOT NULL DEFAULT 0,
    completed_jobs INTEGER NOT NULL DEFAULT 0,
    total_transactions INTEGER NOT NULL DEFAULT 0,
    total_volume DECIMAL(15,2) NOT NULL DEFAULT 0,
    average_job_value DECIMAL(15,2) NOT NULL DEFAULT 0,
    platform_fee DECIMAL(15,2) NOT NULL DEFAULT 0,
    job_completion_rate DECIMAL(5,4) NOT NULL DEFAULT 0,
    average_response_time_hours DECIMAL(8,2) NOT NULL DEFAULT 0,
    user_satisfaction_score DECIMAL(3,2) NOT NULL DEFAULT 0,
    new_users_today INTEGER NOT NULL DEFAULT 0,
    new_jobs_today INTEGER NOT NULL DEFAULT 0,
    revenue_today DECIMAL(15,2) NOT NULL DEFAULT 0
);

-- Create indexes for analytics
CREATE INDEX IF NOT EXISTS idx_job_metrics_tenant ON analytics.job_metrics(tenant_id);
CREATE INDEX IF NOT EXISTS idx_job_metrics_job_id ON analytics.job_metrics(job_id);
CREATE INDEX IF NOT EXISTS idx_job_metrics_client ON analytics.job_metrics(client_id);
CREATE INDEX IF NOT EXISTS idx_job_metrics_worker ON analytics.job_metrics(worker_id);
CREATE INDEX IF NOT EXISTS idx_job_metrics_status ON analytics.job_metrics(status);
CREATE INDEX IF NOT EXISTS idx_job_metrics_category ON analytics.job_metrics(category);
CREATE INDEX IF NOT EXISTS idx_job_metrics_created_at ON analytics.job_metrics(created_at_job);

CREATE INDEX IF NOT EXISTS idx_user_metrics_tenant ON analytics.user_metrics(tenant_id);
CREATE INDEX IF NOT EXISTS idx_user_metrics_user_id ON analytics.user_metrics(user_id);
CREATE INDEX IF NOT EXISTS idx_user_metrics_user_type ON analytics.user_metrics(user_type);
CREATE INDEX IF NOT EXISTS idx_user_metrics_status ON analytics.user_metrics(status);
CREATE INDEX IF NOT EXISTS idx_user_metrics_last_active ON analytics.user_metrics(last_active_at);

CREATE INDEX IF NOT EXISTS idx_platform_metrics_tenant ON analytics.platform_metrics(tenant_id);
CREATE INDEX IF NOT EXISTS idx_platform_metrics_date ON analytics.platform_metrics(metric_date);
CREATE UNIQUE INDEX IF NOT EXISTS idx_platform_metrics_tenant_date ON analytics.platform_metrics(tenant_id, metric_date);
