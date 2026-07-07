package model.entities.unit.strategie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * A strategie that extends Strategie.
 * <p>
 * It is a strategie used to apply an effect to the n closest
 * ennemy or ally depending on the boolean isTargetEnnemy
 */
@Entity
public class NclosestStrategie extends Strategie{

    @Column(name = "is_target_ennemy", nullable = false)
    private boolean isTargetEnnemy;

    @Column(nullable = false)
    private int n;

    public boolean isTargetEnnemy() {
        return isTargetEnnemy;
    }

    public void setTargetEnnemy(boolean targetEnnemy) {
        isTargetEnnemy = targetEnnemy;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }
}
