package com.example.vote.dto;

import java.util.Map;
import java.util.UUID;

public record CountsResponse(UUID pollId, long total, Map<UUID, Long> counts) {
}
