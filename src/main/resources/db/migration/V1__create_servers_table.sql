CREATE TABLE servers
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    owner_id   UUID         NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_servers_owner_id ON servers (owner_id);
