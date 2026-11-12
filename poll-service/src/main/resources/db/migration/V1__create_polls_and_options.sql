CREATE TABLE polls (
    id         UUID         PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    status     VARCHAR(10)  NOT NULL CHECK (status IN ('active', 'closed')),
    ends_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE options (
    id       UUID         PRIMARY KEY,
    poll_id  UUID         NOT NULL REFERENCES polls (id) ON DELETE CASCADE,
    text     VARCHAR(255) NOT NULL,
    position INTEGER      NOT NULL,
    CONSTRAINT uq_options_poll_position UNIQUE (poll_id, position)
);

CREATE INDEX idx_options_poll_id ON options (poll_id);
