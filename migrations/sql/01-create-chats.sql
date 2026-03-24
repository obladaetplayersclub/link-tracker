--liquibase formatted sql

--changeset author:konstantin:create-table-chats
CREATE TABLE chats (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL UNIQUE
);
