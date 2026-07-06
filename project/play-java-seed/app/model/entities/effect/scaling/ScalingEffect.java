package model.entities.effect.scaling;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import model.entities.effect.Effect;
import model.entities.effect.StatType;

@Entity
public abstract class ScalingEffect extends Effect {

    @Column(nullable = false)
    private int base;

    @Column(name = "taype_scaling", nullable = false)
    private StatType typeScaling;

    @Column(name = "coef_scaling", nullable = false)
    private int coefScaling;

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public StatType getTypeScaling() {
        return typeScaling;
    }

    public void setTypeScaling(StatType typeScaling) {
        this.typeScaling = typeScaling;
    }

    public int getCoefScaling() {
        return coefScaling;
    }

    public void setCoefScaling(int coefScaling) {
        this.coefScaling = coefScaling;
    }
}
