package com.example.poll.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "polls")
public class Poll {

    public static final String ACTIVE = "active";
    public static final String CLOSED = "closed";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String status;

    @Column(name = "ends_at")
    private OffsetDateTime endsAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<PollOption> options = new ArrayList<>();

    protected Poll() {
    }

    public Poll(String title, OffsetDateTime endsAt) {
        this.title = title;
        this.endsAt = endsAt;
        this.status = ACTIVE;
        this.createdAt = OffsetDateTime.now();
    }

    public void addOption(PollOption option) {
        option.setPoll(this);
        options.add(option);
    }

    public void close() {
        this.status = CLOSED;
    }

    /** Опрос активен: status = 'active' AND (ends_at IS NULL OR ends_at > now). */
    public boolean isActiveAt(OffsetDateTime now) {
        return ACTIVE.equals(status) && (endsAt == null || endsAt.isAfter(now));
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public OffsetDateTime getEndsAt() { return endsAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public List<PollOption> getOptions() { return options; }
}
