package com.example.poll.repository;

import com.example.poll.entity.Poll;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PollRepository extends JpaRepository<Poll, UUID> {

    @EntityGraph(attributePaths = "options")
    Optional<Poll> findWithOptionsById(UUID id);

    @EntityGraph(attributePaths = "options")
    List<Poll> findAllByOrderByCreatedAtDesc();
}
