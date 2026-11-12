package com.example.vote.controller;

import com.example.vote.dto.CountsResponse;
import com.example.vote.dto.VoteRequest;
import com.example.vote.dto.VoteResponse;
import com.example.vote.service.VoteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private static final Logger log = LoggerFactory.getLogger(VoteController.class);

    private final VoteService service;

    public VoteController(VoteService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VoteResponse vote(@Valid @RequestBody VoteRequest request) {
        log.info("POST /api/votes poll={} voter={}", request.pollId(), request.voterId());
        return service.cast(request);
    }

    @GetMapping("/counts")
    public CountsResponse counts(@RequestParam("poll_id") UUID pollId) {
        log.info("GET /api/votes/counts poll_id={}", pollId);
        return service.counts(pollId);
    }
}
