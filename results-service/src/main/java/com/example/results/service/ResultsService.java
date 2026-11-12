package com.example.results.service;

import com.example.results.client.PollClient;
import com.example.results.client.VoteClient;
import com.example.results.dto.CountsDto;
import com.example.results.dto.PollDto;
import com.example.results.dto.ResultsResponse;
import com.example.results.dto.ResultsResponse.OptionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ResultsService {

    private static final Logger log = LoggerFactory.getLogger(ResultsService.class);

    private final PollClient pollClient;
    private final VoteClient voteClient;

    public ResultsService(PollClient pollClient, VoteClient voteClient) {
        this.pollClient = pollClient;
        this.voteClient = voteClient;
    }

    public ResultsResponse getResults(UUID pollId) {
        PollDto poll = pollClient.getPoll(pollId);          // 404 / 503
        CountsDto votes = voteClient.getCounts(pollId);     // 503
        Map<UUID, Long> counts = votes.counts() == null ? Map.of() : votes.counts();

        // Основа ответа - варианты из Poll Service, поэтому нулевые варианты тоже попадают в ответ.
        long total = poll.options().stream()
                .mapToLong(o -> counts.getOrDefault(o.id(), 0L))
                .sum();

        List<OptionResult> results = poll.options().stream()
                .map(o -> {
                    long count = counts.getOrDefault(o.id(), 0L);
                    return new OptionResult(o.id(), o.text(), count, percent(count, total));
                })
                .toList();

        log.info("Results built: poll={}, total={}", pollId, total);
        return new ResultsResponse(poll.id(), poll.title(), poll.active(), total, results);
    }

    /** Процент с округлением до 1 знака; при нуле голосов всегда 0.0 (без деления на ноль). */
    public static double percent(long count, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(count)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
