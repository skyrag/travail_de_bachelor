package model.entities.effect;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import model.DTO.fighting.EffectAppliedEventDTO;
import model.DTO.fighting.FightingEventDTO;
import model.service.fightingService.ComponentUnit;

/**
 * An effect that has a value and a typeChange that indicates
 * which stat the target of this effect will be affected. It will increase by the value
 */
@Entity
public class StatChangingEffect extends Effect{

    @Column(name = "type_change", nullable = false)
    private StatType typeChange;

    @Column(nullable = false)
    private Integer value;

    protected StatChangingEffect() {
        super();
    }

    public StatChangingEffect(StatType typeChange, int value) {
        super();
        this.typeChange = typeChange;
        this.value = value;
    }

    @Override
    public FightingEventDTO applyTo(ComponentUnit target, ComponentUnit caster, long tick, long abilityId) {
        target.increaseStat(typeChange, value);
        return new EffectAppliedEventDTO(tick, abilityId, target.getId(), caster.getId(), this.getClass().getSimpleName(), value);
    }



    // getter/setter

    public int getValue() {
        return value;
    }

    public StatType getTypeChange() {
        return typeChange;
    }
}
