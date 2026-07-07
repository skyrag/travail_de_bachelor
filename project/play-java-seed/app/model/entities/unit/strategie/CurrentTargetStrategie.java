package model.entities.unit.strategie;

import jakarta.persistence.Entity;

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
}
