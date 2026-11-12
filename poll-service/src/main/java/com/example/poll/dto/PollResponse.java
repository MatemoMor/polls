package com.example.poll.dto;

import com.example.poll.entity.Poll;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PollResponse(
        UUID id,
        String title,
        String status,
        OffsetDateTime endsAt,
        OffsetDateTime createdAt,
        @JsonProperty("is_active") boolean active,
        List<OptionResponse> options
) {
    public static PollResponse from(Poll p, OffsetDateTime now) {
        return new PollResponse(
                p.getId(), p.getTitle(), p.getStatus(), p.getEndsAt(), p.getCreatedAt(),
                p.isActiveAt(now),
                p.getOptions().stream().map(OptionResponse::from).toList());
    }
}
