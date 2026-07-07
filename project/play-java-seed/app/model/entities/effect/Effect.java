package model.entities.effect;

import jakarta.persistence.*;
import model.entities.unit.AbilityFragment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base entity for all effects in the game.
 * <p>
 * An Effect represents a persistable game effect (e.g. damage,
 * heal...) that can be attached to abilities or units.
 * This class only holds the common persistence fields shared by every
 * effect subtype; concrete effects should extend this class and add
 * their own specific behavior and attributes (e.g. base value, scaling
 * stat, coefficient...).
 * <p>
 * Uses InheritanceType#JOINED so each subclass is mapped to its
 * own table, joined on the primary key with this base table.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Effect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
