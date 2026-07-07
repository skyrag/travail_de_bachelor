package model.entities.effect.scaling;

import jakarta.persistence.Entity;
import model.entities.effect.StatType;

/**
 * An effect that extends scalingEffect and that stuns the target
 */
@Entity
public class StunningEffect extends ScalingEffect {

    protected StunningEffect() {
        super();
    }

    public StunningEffect (int base, StatType typeScaling, int coefScaling) {
        super(base, typeScaling, coefScaling);
    }
}
