package com.example.poll.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record CreatePollRequest(
        @NotBlank(message = "title must not be blank")
        @Size(max = 255, message = "title is too long (max 255)")
        String title,

        @Future(message = "ends_at must be in the future")
        OffsetDateTime endsAt,

        @NotNull(message = "options are required")
        @Size(min = 2, max = 10, message = "options must contain from 2 to 10 items")
        List<@NotBlank(message = "option text must not be blank")
             @Size(max = 255, message = "option text is too long (max 255)") String> options
) {
    @JsonIgnore
    @AssertTrue(message = "option texts must be unique within a poll")
    public boolean isOptionsUnique() {
        if (options == null) {
            return true;
        }
        Set<String> seen = new HashSet<>();
        return options.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase())
                .allMatch(seen::add);
    }
}
