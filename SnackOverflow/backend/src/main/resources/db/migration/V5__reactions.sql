CREATE TABLE reactions (
    id          BINARY(16)   NOT NULL,
    user_id     BINARY(16)   NOT NULL,
    target_type VARCHAR(20)  NOT NULL,
    target_id   BINARY(16)   NOT NULL,
    type        VARCHAR(10)  NOT NULL,
    created_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_reaction (user_id, target_type, target_id),
    CONSTRAINT fk_reaction_user FOREIGN KEY (user_id) REFERENCES users (id)
);