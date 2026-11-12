package com.example.results.service;

import com.example.results.client.PollClient;
import com.example.results.client.VoteClient;
import com.example.results.dto.CountsDto;
import com.example.results.dto.PollDto;
import com.example.results.dto.PollDto.OptionDto;
import com.example.results.dto.ResultsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultsServiceTest {

    private static final UUID POLL_ID = UUID.randomUUID();
    private final UUID a = UUID.randomUUID();
    private final UUID b = UUID.randomUUID();
    private final UUID c = UUID.randomUUID();

    @Mock
    PollClient pollClient;

    @Mock
    VoteClient voteClient;

    @InjectMocks
    ResultsService service;

    private PollDto poll() {
        return new PollDto(POLL_ID, "Poll", true,
                List.of(new OptionDto(a, "A"), new OptionDto(b, "B"), new OptionDto(c, "C")));
    }

    @Test
    void zeroVotesGivesZeroPercentAndAllOptions() {
        when(pollClient.getPoll(POLL_ID)).thenReturn(poll());
        when(voteClient.getCounts(POLL_ID)).thenReturn(new CountsDto(POLL_ID, 0, Map.of()));

        ResultsResponse r = service.getResults(POLL_ID);

        assertEquals(0, r.totalVotes());
        assertEquals(3, r.results().size());
        r.results().forEach(o -> {
            assertEquals(0, o.count());
            assertEquals(0.0, o.percent());
        });
    }

    @Test
    void percentsAreRoundedToOneDecimal() {
        when(pollClient.getPoll(POLL_ID)).thenReturn(poll());
        when(voteClient.getCounts(POLL_ID)).thenReturn(new CountsDto(POLL_ID, 3, Map.of(a, 2L, b, 1L)));

        ResultsResponse r = service.getResults(POLL_ID);

        assertEquals(3, r.totalVotes());
        assertEquals(66.7, r.results().get(0).percent());
        assertEquals(33.3, r.results().get(1).percent());
        assertEquals(0, r.results().get(2).count());   // вариант без голосов остаётся в ответе
        assertEquals(0.0, r.results().get(2).percent());
    }

    @Test
    void percentFunctionEdgeCases() {
        assertEquals(0.0, ResultsService.percent(0, 0));
        assertEquals(0.0, ResultsService.percent(5, 0));
        assertEquals(100.0, ResultsService.percent(4, 4));
        assertEquals(12.5, ResultsService.percent(1, 8));
        assertEquals(16.7, ResultsService.percent(1, 6));
        assertEquals(33.3, ResultsService.percent(1, 3));
        assertEquals(60.0, ResultsService.percent(3, 5));
        assertEquals(20.0, ResultsService.percent(1, 5));
    }
}
