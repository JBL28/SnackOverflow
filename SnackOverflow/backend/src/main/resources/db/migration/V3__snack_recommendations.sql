CREATE TABLE snack_recommendations (
    id          BINARY(16)   NOT NULL,
    name        VARCHAR(100) NOT NULL,
    reason      TEXT         NOT NULL,
    likes       INT          NOT NULL DEFAULT 0,
    dislikes    INT          NOT NULL DEFAULT 0,
    created_by  BINARY(16)   NOT NULL,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_rec_user FOREIGN KEY (created_by) REFERENCES users (id)
);
