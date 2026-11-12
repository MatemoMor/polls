package com.example.poll.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "options")
public class PollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private int position;

    protected PollOption() {
    }

    public PollOption(String text, int position) {
        this.text = text;
        this.position = position;
    }

    void setPoll(Poll poll) { this.poll = poll; }

    public UUID getId() { return id; }
    public Poll getPoll() { return poll; }
    public String getText() { return text; }
    public int getPosition() { return position; }
}
