package com.example.vote.dto;

import com.example.vote.entity.Vote;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VoteResponse(UUID id, UUID pollId, UUID optionId, String voterId, OffsetDateTime createdAt) {
    public static VoteResponse from(Vote v) {
        return new VoteResponse(v.getId(), v.getPollId(), v.getOptionId(), v.getVoterId(), v.getCreatedAt());
    }
}
