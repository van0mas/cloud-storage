--liquibase formatted sql
--changeset gofra:1

CREATE TABLE files (
                       id BIGSERIAL PRIMARY KEY,
                       user_id BIGINT NOT NULL,
                       path TEXT NOT NULL,
                       name VARCHAR(255) NOT NULL,
                       size BIGINT NOT NULL,
                       is_dir BOOLEAN NOT NULL DEFAULT FALSE,
                       content_type VARCHAR(100),
                       created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
                       CONSTRAINT uk_user_path UNIQUE (user_id, path)
);

CREATE INDEX idx_files_user_id ON files(user_id);