--liquibase formatted sql

--changeset konstantin:create-table-tags
CREATE TABLE tags(
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);
