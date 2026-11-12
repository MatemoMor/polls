package com.example.poll.service;

import com.example.poll.dto.CreatePollRequest;
import com.example.poll.dto.PollResponse;
import com.example.poll.entity.Poll;
import com.example.poll.entity.PollOption;
import com.example.poll.exception.NotFoundException;
import com.example.poll.repository.PollRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PollService {

    private static final Logger log = LoggerFactory.getLogger(PollService.class);

    private final PollRepository repository;

    public PollService(PollRepository repository) {
        this.repository = repository;
    }

    /** Опрос и все его варианты сохраняются в одной транзакции. */
    @Transactional
    public PollResponse create(CreatePollRequest request) {
        Poll poll = new Poll(request.title().trim(), request.endsAt());
        int position = 1;
        for (String text : request.options()) {
            poll.addOption(new PollOption(text.trim(), position++));
        }
        Poll saved = repository.save(poll);
        log.info("Poll created: id={}, options={}", saved.getId(), saved.getOptions().size());
        return PollResponse.from(saved, OffsetDateTime.now());
    }

    @Transactional(readOnly = true)
    public PollResponse get(UUID id) {
        Poll poll = repository.findWithOptionsById(id)
                .orElseThrow(() -> new NotFoundException("Poll " + id + " not found"));
        return PollResponse.from(poll, OffsetDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<PollResponse> list() {
        OffsetDateTime now = OffsetDateTime.now();
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(p -> PollResponse.from(p, now))
                .toList();
    }

    @Transactional
    public PollResponse close(UUID id) {
        Poll poll = repository.findWithOptionsById(id)
                .orElseThrow(() -> new NotFoundException("Poll " + id + " not found"));
        poll.close();
        log.info("Poll closed: id={}", id);
        return PollResponse.from(poll, OffsetDateTime.now());
    }
}
