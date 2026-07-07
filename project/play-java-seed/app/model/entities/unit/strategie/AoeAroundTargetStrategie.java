package model.entities.unit.strategie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * A strategie that extends Strategie.
 * <p>
 * It is a strategie used to apply an effect to a target and
 * any other target in a circleof size cells around the target,
 * which can be you if isCenterYou is true.
 * The effect only targets ennemy or ally based on isTargetEnnemy
 */
@Entity
public class AoeAroundTargetStrategie extends Strategie{

    @Column(name = "is_target_ennemy", nullable = false)
    private boolean isTargetEnnemy;

    @Column(name = "is_center_you", nullable = false)
    private boolean isCenterYou;

    @Column(nullable = false)
    private int size;

    public boolean isTargetEnnemy() {
        return isTargetEnnemy;
    }

    public void setTargetEnnemy(boolean targetEnnemy) {
        isTargetEnnemy = targetEnnemy;
    }

    public int getN() {
        return size;
    }

    public void setN(int size) {
        this.size = size;
    }
}
