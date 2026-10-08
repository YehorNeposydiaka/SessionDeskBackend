CREATE TABLE refresh_tokens (
    id UUID NOT NULL,
    specialist_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_refresh_tokens_specialist
        FOREIGN KEY (specialist_id)
        REFERENCES specialists (id)
);