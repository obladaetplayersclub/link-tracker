--liquibase formatted sql

--changeset konstantin:create-indexes
CREATE INDEX IF NOT EXISTS idx_chat_links_chat_id ON chat_links(chat_id);
CREATE INDEX IF NOT EXISTS idx_chat_links_link_id ON chat_links(link_id);
CREATE INDEX IF NOT EXISTS idx_link_tags_link_id ON link_tags(link_id);
CREATE INDEX IF NOT EXISTS idx_link_tags_tag_id ON link_tags(tag_id);
