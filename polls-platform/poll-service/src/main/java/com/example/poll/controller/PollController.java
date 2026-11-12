package com.example.poll.controller;

import com.example.poll.dto.CreatePollRequest;
import com.example.poll.dto.PollResponse;
import com.example.poll.service.PollService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/polls")
public class PollController {

    private static final Logger log = LoggerFactory.getLogger(PollController.class);

    private final PollService service;

    public PollController(PollService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PollResponse create(@Valid @RequestBody CreatePollRequest request) {
        log.info("POST /api/polls title='{}'", request.title());
        return service.create(request);
    }

    @GetMapping("/{id}")
    public PollResponse get(@PathVariable UUID id) {
        log.info("GET /api/polls/{}", id);
        return service.get(id);
    }

    @GetMapping
    public List<PollResponse> list() {
        log.info("GET /api/polls");
        return service.list();
    }

    @PostMapping("/{id}/close")
    public PollResponse close(@PathVariable UUID id) {
        log.info("POST /api/polls/{}/close", id);
        return service.close(id);
    }
}
