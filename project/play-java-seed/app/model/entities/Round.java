package model.entities;

import jakarta.persistence.*;
import model.entities.event.Event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single round played by a Team within a game.
 * <p>
 * A Round is identified by its round number (the
 * round's position in the overall game sequence) and holds the
 * ordered list of Events that occurred during it (e.g.
 * bought unit, reroll shop...), used to reconstruct/replay
 * what happened.
 */
@Entity
@Table(name = "round")
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRoundNumber() {
        return round;
    }

    public void setRoundNumber(Integer round) {
        this.round = round;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
