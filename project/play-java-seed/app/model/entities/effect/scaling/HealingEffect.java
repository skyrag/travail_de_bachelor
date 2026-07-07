package model.entities.effect.scaling;

import jakarta.persistence.Entity;
import model.entities.effect.StatType;

/**
 * An effect that extends scalingEffect and that heals the target
 */
@Entity
public class HealingEffect extends ScalingEffect {

    protected HealingEffect() {
        super();
    }

    public HealingEffect (int base, StatType typeScaling, int coefScaling) {
        super(base, typeScaling, coefScaling);
    }
}
