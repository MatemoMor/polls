package com.example.poll.controller;

import com.example.poll.dto.PollResponse;
import com.example.poll.service.PollService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PollController.class)
class PollControllerValidationTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PollService service;

    private static String body(String title, List<String> options) {
        String opts = options.stream().map(o -> "\"" + o + "\"").collect(Collectors.joining(","));
        return "{\"title\":\"" + title + "\",\"options\":[" + opts + "]}";
    }

    private ResultActions send(String json) throws Exception {
        return mvc.perform(post("/api/polls").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void emptyTitleIsRejected() throws Exception {
        send(body("", List.of("A", "B")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").exists());
        verify(service, never()).create(any());
    }

    @Test
    void singleOptionIsRejected() throws Exception {
        send(body("Title", List.of("A")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").exists());
        verify(service, never()).create(any());
    }

    @Test
    void elevenOptionsAreRejected() throws Exception {
        List<String> eleven = IntStream.rangeClosed(1, 11).mapToObj(i -> "Option " + i).toList();
        send(body("Title", eleven))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").exists());
        verify(service, never()).create(any());
    }

    @Test
    void duplicateOptionTextsAreRejected() throws Exception {
        send(body("Title", List.of("Same", "same ", "Other")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").exists());
        verify(service, never()).create(any());
    }

    @Test
    void blankOptionTextIsRejected() throws Exception {
        send(body("Title", List.of("A", " ")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void validRequestIsAccepted() throws Exception {
        when(service.create(any())).thenReturn(new PollResponse(
                UUID.randomUUID(), "Title", "active", null, OffsetDateTime.now(), true, List.of()));
        send(body("Title", List.of("A", "B", "C")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.is_active").value(true));
    }
}
