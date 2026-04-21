--liquibase formatted sql

--changeset konstantin:create-table-links
CREATE TABLE IF NOT EXISTS links (
    id BIGSERIAL PRIMARY KEY,
    url TEXT NOT NULL UNIQUE,
    last_updated TIMESTAMPTZ DEFAULT NOW()
);
