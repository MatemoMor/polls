package com.example.poll.dto;

import com.example.poll.entity.PollOption;

import java.util.UUID;

public record OptionResponse(UUID id, String text, int position) {
    public static OptionResponse from(PollOption o) {
        return new OptionResponse(o.getId(), o.getText(), o.getPosition());
    }
}
