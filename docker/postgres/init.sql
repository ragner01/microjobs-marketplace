-- Create databases for different services
CREATE DATABASE microjobs;
CREATE DATABASE keycloak;

-- Create users for different services
CREATE USER microjobs WITH PASSWORD 'microjobs123';
CREATE USER keycloak WITH PASSWORD 'keycloak123';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE microjobs TO microjobs;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;

-- Connect to microjobs database and create schemas
\c microjobs;

-- Create schemas for multi-tenancy
CREATE SCHEMA IF NOT EXISTS jobs;
CREATE SCHEMA IF NOT EXISTS escrow;
CREATE SCHEMA IF NOT EXISTS shared;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant schema permissions to microjobs user
GRANT ALL ON SCHEMA jobs TO microjobs;
GRANT ALL ON SCHEMA escrow TO microjobs;
GRANT ALL ON SCHEMA shared TO microjobs;

-- Connect to keycloak database
\c keycloak;

-- Grant privileges to keycloak user
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
GRANT ALL ON SCHEMA public TO keycloak;
