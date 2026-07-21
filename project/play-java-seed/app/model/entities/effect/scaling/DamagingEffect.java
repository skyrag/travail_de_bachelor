package model.entities.effect.scaling;

import jakarta.persistence.Entity;
import model.DTO.fighting.EffectAppliedEventDTO;
import model.DTO.fighting.FightingEventDTO;
import model.entities.effect.StatType;
import model.service.fightingService.ComponentUnit;

/**
 * An effect that extends ScalingEffect and that damages the target
 */
@Entity
public class DamagingEffect extends ScalingEffect {

    protected DamagingEffect() {
        super();
    }

    @Override
    public FightingEventDTO applyTo(ComponentUnit target, ComponentUnit caster, long tick, long abilityId) {
        int trueDamage = Math.toIntExact(Math.round(this.getBase() + (this.getCoefScaling()/100.0 * caster.getStat(this.getTypeScaling()))));
        int mitigatedDamage = target.damageMagic(trueDamage);
        return new EffectAppliedEventDTO(tick,abilityId, target.getId(), caster.getId(), this.getClass().getSimpleName(), mitigatedDamage);
    }

    public DamagingEffect (int base, StatType typeScaling, int coefScaling) {
        super(base, typeScaling, coefScaling);
    }


}
