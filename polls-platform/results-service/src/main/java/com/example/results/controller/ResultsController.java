package com.example.results.controller;

import com.example.results.dto.ResultsResponse;
import com.example.results.service.ResultsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/results")
public class ResultsController {

    private static final Logger log = LoggerFactory.getLogger(ResultsController.class);

    private final ResultsService service;

    public ResultsController(ResultsService service) {
        this.service = service;
    }

    @GetMapping("/{pollId}")
    public ResultsResponse results(@PathVariable UUID pollId) {
        log.info("GET /api/results/{}", pollId);
        return service.getResults(pollId);
    }
}
