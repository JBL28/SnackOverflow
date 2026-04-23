CREATE TABLE users
(
    id                   BINARY(16)   NOT NULL,
    username             VARCHAR(20)  NOT NULL,
    nickname             VARCHAR(30)  NOT NULL,
    email                VARCHAR(100) NOT NULL,
    password_hash        VARCHAR(255) NOT NULL,
    role                 VARCHAR(10)  NOT NULL DEFAULT 'USER',
    status               VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    post_count           INT          NOT NULL DEFAULT 0,
    comment_count        INT          NOT NULL DEFAULT 0,
    must_change_password BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           DATETIME     NOT NULL,
    updated_at           DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'))
);

CREATE TABLE refresh_tokens
(
    id         BINARY(16)  NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    user_id    BINARY(16)  NOT NULL,
    family_id  BINARY(16)  NOT NULL,
    expires_at DATETIME    NOT NULL,
    revoked    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at DATETIME    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_token_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_token_family_id ON refresh_tokens (family_id);
