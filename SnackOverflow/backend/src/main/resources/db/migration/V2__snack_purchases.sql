CREATE TABLE snack_purchases
(
    id         BINARY(16)   NOT NULL,
    name       VARCHAR(100) NOT NULL,
    status     VARCHAR(15)  NOT NULL DEFAULT 'IN_STOCK',
    likes      INT          NOT NULL DEFAULT 0,
    dislikes   INT          NOT NULL DEFAULT 0,
    created_by BINARY(16)   NOT NULL,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT chk_snack_purchase_status CHECK (status IN ('DELIVERING', 'IN_STOCK', 'OUT_OF_STOCK')),
    CONSTRAINT fk_snack_purchase_creator FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE INDEX idx_snack_purchase_status ON snack_purchases (status);
CREATE INDEX idx_snack_purchase_created_at ON snack_purchases (created_at);
