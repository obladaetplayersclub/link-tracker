--liquibase formatted sql

--changeset konstantin:create-table-chat_links
CREATE TABLE chat_links(
    chat_id BIGINT REFERENCES chats(id) ON DELETE CASCADE,
    link_id BIGINT REFERENCES links(id) ON DELETE CASCADE,
    PRIMARY KEY (chat_id , link_id)
);
