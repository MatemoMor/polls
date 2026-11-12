package com.example.vote.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "votes")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "poll_id", nullable = false)
    private UUID pollId;

    @Column(name = "option_id", nullable = false)
    private UUID optionId;

    @Column(name = "voter_id", nullable = false)
    private String voterId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Vote() {
    }

    public Vote(UUID pollId, UUID optionId, String voterId) {
        this.pollId = pollId;
        this.optionId = optionId;
        this.voterId = voterId;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getPollId() { return pollId; }
    public UUID getOptionId() { return optionId; }
    public String getVoterId() { return voterId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
