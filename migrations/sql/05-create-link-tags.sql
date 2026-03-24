--liquibase formatted sql

--changeset konstantin:create-table-link-tags
CREATE TABLE link_tags(
    link_id BIGINT REFERENCES links(id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (link_id, tag_id)
);
