package model.entities.effect.scaling;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import model.entities.effect.Effect;
import model.entities.effect.StatType;

/**
 * An effect that has a base value, a typeScaling that indicates which
 * stat of the caster is used to scale this effect, and a coefScaling
 * that defines the coefficient applied to that stat when computing
 * the effect's final value.
 */
@Entity
public abstract class ScalingEffect extends Effect {

    @Column(nullable = false)
    private Integer base;

    @Column(name = "taype_scaling", nullable = false)
    private StatType typeScaling;

    @Column(name = "coef_scaling", nullable = false)
    private Integer coefScaling;

    protected ScalingEffect(){
        super();
    }

    protected ScalingEffect (int base, StatType typeScaling, int coefScaling) {
        super();
        this.base = base;
        this.typeScaling = typeScaling;
        this.coefScaling = coefScaling;
    }


    // getter/setter

    public int getBase() {
        return base;
    }

    public StatType getTypeScaling() {
        return typeScaling;
    }

    public int getCoefScaling() {
        return coefScaling;
    }
}
