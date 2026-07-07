package model.entities.event;

import jakarta.persistence.*;
import model.entities.Round;

import java.time.LocalDateTime;

/**
 * Base entity for all events that occur during a Round.
 * <p>
 * An Event represents something that happened at a specific
 * step within a round (e.g. buying a unit, rerolling the shop...).
 * This class only holds the common persistence
 * fields shared by every event subtype; concrete events should extend
 * this class and add their own specific data.
 * <p>
 * Uses InheritanceType#JOINED so each subclass is mapped to its
 * own table, joined on the primary key with this base table.
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round; // nécessaire pour hibernate

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    protected Event(){

    }

    protected Event(int step, Round round){
        this.step = step;
        this.round = round;
    }


    // getter/setter

    public long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Round getRound() {
        return round;
    }

    public int getStep() {
        return step;
    }
}
