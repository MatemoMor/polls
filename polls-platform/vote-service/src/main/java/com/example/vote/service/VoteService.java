package com.example.vote.service;

import com.example.vote.client.PollClient;
import com.example.vote.dto.CountsResponse;
import com.example.vote.dto.PollDto;
import com.example.vote.dto.VoteRequest;
import com.example.vote.dto.VoteResponse;
import com.example.vote.entity.Vote;
import com.example.vote.exception.ConflictException;
import com.example.vote.exception.UnprocessableException;
import com.example.vote.repository.VoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class VoteService {

    private static final Logger log = LoggerFactory.getLogger(VoteService.class);

    private final VoteRepository repository;
    private final PollClient pollClient;

    public VoteService(VoteRepository repository, PollClient pollClient) {
        this.repository = repository;
        this.pollClient = pollClient;
    }

    /**
     * Метод намеренно без @Transactional: HTTP-вызов не должен держать соединение с БД,
     * а saveAndFlush выполняется в транзакции репозитория, поэтому нарушение
     * UNIQUE (poll_id, voter_id) возникает прямо здесь и ловится ниже.
     * Защита от повторного голосования только ограничением БД (без select перед insert).
     */
    public VoteResponse cast(VoteRequest request) {
        PollDto poll = pollClient.getPoll(request.pollId());

        if (!poll.active()) {
            throw new UnprocessableException("Poll " + request.pollId() + " is closed");
        }
        boolean optionBelongs = poll.options().stream().anyMatch(o -> request.optionId().equals(o.id()));
        if (!optionBelongs) {
            throw new UnprocessableException(
                    "Option " + request.optionId() + " does not belong to poll " + request.pollId());
        }

        try {
            Vote saved = repository.saveAndFlush(
                    new Vote(request.pollId(), request.optionId(), request.voterId()));
            log.info("Vote accepted: poll={}, option={}, voter={}",
                    request.pollId(), request.optionId(), request.voterId());
            return VoteResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate vote: poll={}, voter={}", request.pollId(), request.voterId());
            throw new ConflictException(
                    "Voter " + request.voterId() + " has already voted in poll " + request.pollId());
        }
    }

    @Transactional(readOnly = true)
    public CountsResponse counts(UUID pollId) {
        Map<UUID, Long> counts = new LinkedHashMap<>();
        long total = 0;
        for (VoteRepository.OptionCount row : repository.countByOption(pollId)) {
            counts.put(row.getOptionId(), row.getCnt());
            total += row.getCnt();
        }
        return new CountsResponse(pollId, total, counts);
    }
}
