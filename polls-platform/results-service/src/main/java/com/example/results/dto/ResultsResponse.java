package com.example.results.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ResultsResponse(
        UUID pollId,
        String title,
        @JsonProperty("is_active") boolean active,
        long totalVotes,
        List<OptionResult> results
) {
    public record OptionResult(UUID optionId, String text, long count, double percent) {
    }
}
