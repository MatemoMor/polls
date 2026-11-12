package com.example.vote.controller;

import com.example.vote.client.PollClient;
import com.example.vote.dto.PollDto;
import com.example.vote.entity.Vote;
import com.example.vote.exception.NotFoundException;
import com.example.vote.exception.ServiceUnavailableException;
import com.example.vote.repository.VoteRepository;
import com.example.vote.service.VoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoteController.class)
@Import(VoteService.class)
class VoteControllerTest {

    private static final UUID POLL_ID = UUID.randomUUID();
    private static final UUID OPTION_ID = UUID.randomUUID();

    @Autowired
    MockMvc mvc;

    @MockBean
    VoteRepository repository;

    @MockBean
    PollClient pollClient;

    private ResultActions vote(UUID optionId) throws Exception {
        String json = "{\"poll_id\":\"" + POLL_ID + "\",\"option_id\":\"" + optionId
                + "\",\"voter_id\":\"voter-1\"}";
        return mvc.perform(post("/api/votes").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private PollDto poll(boolean active) {
        return new PollDto(POLL_ID, "Title", active, List.of(new PollDto.OptionDto(OPTION_ID, "A")));
    }

    @Test
    void duplicateVoteReturns409() throws Exception {
        when(pollClient.getPoll(POLL_ID)).thenReturn(poll(true));
        when(repository.saveAndFlush(any(Vote.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates uq_votes_poll_voter"));

        vote(OPTION_ID)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void validVoteReturns201() throws Exception {
        when(pollClient.getPoll(POLL_ID)).thenReturn(poll(true));
        when(repository.saveAndFlush(any(Vote.class))).thenAnswer(inv -> inv.getArgument(0));

        vote(OPTION_ID)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voter_id").value("voter-1"));
    }

    @Test
    void closedPollReturns422() throws Exception {
        when(pollClient.getPoll(POLL_ID)).thenReturn(poll(false));

        vote(OPTION_ID).andExpect(status().isUnprocessableEntity());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void foreignOptionReturns422() throws Exception {
        when(pollClient.getPoll(POLL_ID)).thenReturn(poll(true));

        vote(UUID.randomUUID()).andExpect(status().isUnprocessableEntity());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void unknownPollReturns404() throws Exception {
        when(pollClient.getPoll(POLL_ID)).thenThrow(new NotFoundException("Poll not found"));

        vote(OPTION_ID).andExpect(status().isNotFound());
    }

    @Test
    void pollServiceDownReturns503() throws Exception {
        when(pollClient.getPoll(POLL_ID))
                .thenThrow(new ServiceUnavailableException("Poll Service is unavailable", null));

        vote(OPTION_ID).andExpect(status().isServiceUnavailable());
    }
}
