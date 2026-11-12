package com.example.results.client;

import com.example.results.dto.CountsDto;
import com.example.results.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class VoteClient {

    private static final Logger log = LoggerFactory.getLogger(VoteClient.class);

    private final RestClient restClient;

    public VoteClient(@Qualifier("voteRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public CountsDto getCounts(UUID pollId) {
        try {
            CountsDto counts = restClient.get()
                    .uri(uri -> uri.path("/api/votes/counts").queryParam("poll_id", pollId).build())
                    .retrieve()
                    .body(CountsDto.class);
            if (counts == null) {
                throw new ServiceUnavailableException("Vote Service returned an empty response", null);
            }
            return counts;
        } catch (RestClientException e) {
            log.warn("Vote Service call failed for poll {}: {}", pollId, e.getMessage());
            throw new ServiceUnavailableException("Vote Service is unavailable", e);
        }
    }
}
