package com.example.vote.repository;

import com.example.vote.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {

    interface OptionCount {
        UUID getOptionId();
        Long getCnt();
    }

    @Query("select v.optionId as optionId, count(v) as cnt from Vote v where v.pollId = :pollId group by v.optionId")
    List<OptionCount> countByOption(@Param("pollId") UUID pollId);
}
