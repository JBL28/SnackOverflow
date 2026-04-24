CREATE TABLE comments (
    id           BINARY(16)   NOT NULL,
    content      TEXT         NOT NULL,
    author_id    BINARY(16)   NOT NULL,
    parent_id    BINARY(16)   NULL,
    target_type  VARCHAR(20)  NOT NULL,
    target_id    BINARY(16)   NOT NULL,
    likes        INT          NOT NULL DEFAULT 0,
    dislikes     INT          NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comments (id)
);