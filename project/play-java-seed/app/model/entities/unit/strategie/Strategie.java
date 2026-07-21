package model.entities.unit.strategie;

import jakarta.persistence.*;
import model.service.fightingService.ComponentUnit;
import model.service.fightingService.FightingContext;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Base entity for all strategie used to apply effect.
 * <p>
 * A Strategie is used when we want to apply an effect
 * to a specific set of target based on the strategie used
 * <p>
 * Uses InheritanceType#JOINED so each subclass is mapped to its
 * own table, joined on the primary key with this base table.
 */
@Entity
@Table(name = "strategie")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Strategie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    protected Strategie() {

    }

    public abstract List<ComponentUnit> findTarget(ComponentUnit caster, FightingContext context);

    //getter/setter
    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
