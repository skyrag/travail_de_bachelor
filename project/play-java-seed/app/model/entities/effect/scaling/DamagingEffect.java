package model.entities.effect.scaling;

import jakarta.persistence.Entity;
import model.entities.effect.StatType;

/**
 * An effect that extends ScalingEffect and that damages the target
 */
@Entity
public class DamagingEffect extends ScalingEffect {

    protected DamagingEffect() {
        super();
    }

    public DamagingEffect (int base, StatType typeScaling, int coefScaling) {
        super(base, typeScaling, coefScaling);
    }
}
