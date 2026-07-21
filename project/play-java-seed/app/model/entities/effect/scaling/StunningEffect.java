package model.entities.effect.scaling;

import jakarta.persistence.Entity;
import model.DTO.fighting.EffectAppliedEventDTO;
import model.DTO.fighting.FightingEventDTO;
import model.entities.effect.StatType;
import model.service.fightingService.ComponentUnit;
import model.service.fightingService.StatusType;

/**
 * An effect that extends scalingEffect and that stuns the target
 */
@Entity
public class StunningEffect extends ScalingEffect {

    protected StunningEffect() {
        super();
    }

    @Override
    public FightingEventDTO applyTo(ComponentUnit target, ComponentUnit caster, long tick, long abilityId) {
        int duration = Math.toIntExact(Math.round(this.getBase() + (this.getCoefScaling()/100.0 * caster.getStat(this.getTypeScaling()))));
        target.applyStatus(StatusType.STUN, duration);
        return new EffectAppliedEventDTO(tick, abilityId, target.getId(), caster.getId(), this.getClass().getSimpleName(), duration);
    }

    public StunningEffect (int base, StatType typeScaling, int coefScaling) {
        super(base, typeScaling, coefScaling);
    }
}
