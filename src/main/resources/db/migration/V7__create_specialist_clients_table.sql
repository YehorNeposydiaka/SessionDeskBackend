CREATE TABLE specialist_clients (
    id UUID PRIMARY KEY,
    specialist_id UUID NOT NULL,
    client_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_specialist_clients_specialist
        FOREIGN KEY (specialist_id) REFERENCES specialists(id),

    CONSTRAINT fk_specialist_clients_client
        FOREIGN KEY (client_id) REFERENCES clients(id),

    CONSTRAINT uk_specialist_clients_specialist_client
        UNIQUE (specialist_id, client_id),

    CONSTRAINT chk_specialist_clients_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'PAUSED'))
);

CREATE INDEX idx_specialist_clients_specialist_id
    ON specialist_clients(specialist_id);

CREATE INDEX idx_specialist_clients_client_id
    ON specialist_clients(client_id);