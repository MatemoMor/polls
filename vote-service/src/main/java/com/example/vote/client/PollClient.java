package com.example.vote.client;

import com.example.vote.dto.PollDto;
import com.example.vote.exception.NotFoundException;
import com.example.vote.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class PollClient {

    private static final Logger log = LoggerFactory.getLogger(PollClient.class);

    private final RestClient restClient;

    public PollClient(RestClient pollRestClient) {
        this.restClient = pollRestClient;
    }

    /**
     * 404 от Poll Service -> NotFoundException;
     * таймаут, ошибка сети, 5xx, неожиданный ответ -> ServiceUnavailableException (503).
     */
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
