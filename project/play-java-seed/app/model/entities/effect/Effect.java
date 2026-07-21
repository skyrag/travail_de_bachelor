package model.entities.effect;

import jakarta.persistence.*;
import model.DTO.fighting.FightingEventDTO;
import model.entities.unit.AbilityFragment;
import model.service.fightingService.ComponentUnit;

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
    private Long id;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    protected Effect () {

    }

    public abstract FightingEventDTO applyTo(ComponentUnit target, ComponentUnit caster, long tick, long abilityId);

    // getter/setter

    public long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
