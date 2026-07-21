package model.entities.unit.strategie;

import jakarta.persistence.Entity;
import model.service.fightingService.ComponentUnit;
import model.service.fightingService.FightingContext;

import java.util.List;

/**
 * A strategie that extends Strategie.
 * <p>
 * It is a strategie used to apply an effect only on the current target of the caster
 */
@Entity
public class CurrentTargetStrategie extends Strategie{

    public CurrentTargetStrategie(){
        super();
    }

    @Override
    public List<ComponentUnit> findTarget(ComponentUnit caster, FightingContext context) {
        return List.of(caster.currentEnnemy);
    }
}
