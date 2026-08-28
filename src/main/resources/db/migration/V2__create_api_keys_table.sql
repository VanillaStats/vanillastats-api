CREATE TABLE api_keys
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    server_id  UUID         NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    key_hash   VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_api_keys_server_id ON api_keys (server_id);
CREATE UNIQUE INDEX idx_api_keys_key_hash ON api_keys (key_hash);
