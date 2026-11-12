package com.example.results.client;

import com.example.results.dto.PollDto;
import com.example.results.exception.NotFoundException;
import com.example.results.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class PollClient {

    private static final Logger log = LoggerFactory.getLogger(PollClient.class);

    private final RestClient restClient;

    public PollClient(@Qualifier("pollRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public PollDto getPoll(UUID pollId) {
        try {
            PollDto poll = restClient.get()
                    .uri("/api/polls/{id}", pollId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new NotFoundException("Poll " + pollId + " not found");
                    })
                    .body(PollDto.class);
            if (poll == null || poll.options() == null) {
                throw new ServiceUnavailableException("Poll Service returned an empty response", null);
            }
            return poll;
        } catch (RestClientException e) {
            log.warn("Poll Service call failed for poll {}: {}", pollId, e.getMessage());
            throw new ServiceUnavailableException("Poll Service is unavailable", e);
        }
    }
}
