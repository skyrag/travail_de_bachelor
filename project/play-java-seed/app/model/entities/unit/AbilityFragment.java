package model.entities.unit;

import jakarta.persistence.*;
import model.entities.effect.Effect;
import model.entities.unit.strategie.Strategie;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represent one of the part of the ability from a unit.
 * <p>
 * It has a list of effect to apply when casting the ability,
 * and it also has a strategie that helps to determine the target of the effects.
 * We fragment the ability's effect so that we can have multiple effect that applies on
 * different target for one ability.
 */
@Entity
@Table(name = "ability_fragment")
public class AbilityFragment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategie_id", nullable = false)
    private Strategie strategie;

    @ManyToMany
    @JoinTable(
            name = "abilitys_effect",
            joinColumns = @JoinColumn(name = "ability_fragment_id"),
            inverseJoinColumns = @JoinColumn(name = "effect_id")
    )
    private List<Effect> effects = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;  // champ obligatoire pour Hibernate

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    protected AbilityFragment() {
    }

    public AbilityFragment(Strategie strategie, List<Effect> effects, Unit unit){
        this.strategie = strategie;
        this.effects = effects;
        this.unit = unit;
    }


    //getter/setter
    public Long getId() {
        return id;
    }

    public Strategie getStrategie() {
        return strategie;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Effect> getEffects() {
        return effects;
    }
}
