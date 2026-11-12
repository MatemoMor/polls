package com.example.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record VoteRequest(
        @NotNull(message = "poll_id is required") UUID pollId,
        @NotNull(message = "option_id is required") UUID optionId,
        @NotBlank(message = "voter_id must not be blank")
        @Size(max = 255, message = "voter_id is too long (max 255)") String voterId
) {
}
