package com.example.vote.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/** Ответ Poll Service, нужная нам часть. */
public record PollDto(UUID id, String title, @JsonProperty("is_active") boolean active, List<OptionDto> options) {

    public record OptionDto(UUID id, String text) {
    }
}
