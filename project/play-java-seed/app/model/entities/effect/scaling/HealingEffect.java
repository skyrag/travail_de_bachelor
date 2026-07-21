package model.entities.effect.scaling;

import jakarta.persistence.Entity;
import model.DTO.fighting.EffectAppliedEventDTO;
import model.DTO.fighting.FightingEventDTO;
import model.entities.effect.StatType;
import model.service.fightingService.ComponentUnit;

/**
 * An effect that extends scalingEffect and that heals the target
 */
@Entity
public class HealingEffect extends ScalingEffect {

    protected HealingEffect() {
        super();
    }

    @Override
    public FightingEventDTO applyTo(ComponentUnit target, ComponentUnit caster, long tick, long abilityId) {
        int value = Math.toIntExact(Math.round(this.getBase() + (this.getCoefScaling()/100.0 * caster.getStat(this.getTypeScaling()))));
        target.heal(value);
        return new EffectAppliedEventDTO(tick, abilityId, target.getId(), caster.getId(), this.getClass().getSimpleName(), value);
    }

    public HealingEffect (int base, StatType typeScaling, int coefScaling) {
        super(base, typeScaling, coefScaling);
    }
}
