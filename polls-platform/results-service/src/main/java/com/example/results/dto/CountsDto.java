package com.example.results.dto;

import java.util.Map;
import java.util.UUID;

public record CountsDto(UUID pollId, long total, Map<UUID, Long> counts) {
}
