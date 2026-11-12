CREATE TABLE votes (
    id         UUID         PRIMARY KEY,
    poll_id    UUID         NOT NULL,
    option_id  UUID         NOT NULL,
    voter_id   VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_votes_poll_voter UNIQUE (poll_id, voter_id)
);

CREATE INDEX idx_votes_poll_option ON votes (poll_id, option_id);
