package model.entities.unit.strategie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

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
