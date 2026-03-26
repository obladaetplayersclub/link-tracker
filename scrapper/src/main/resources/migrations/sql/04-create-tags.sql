--liquibase formatted sql

--changeset konstantin:create-table-tags
CREATE TABLE IF NOT EXISTS tags(
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);
