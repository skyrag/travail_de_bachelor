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
    private Boolean isTargetEnnemy;

    @Column(name = "is_center_you", nullable = false)
    private Boolean isCenterYou;

    @Column(nullable = false)
    private Integer size;

    protected AoeAroundTargetStrategie(){
        super();
    }

    public AoeAroundTargetStrategie(boolean isTargetEnnemy, boolean isCenterYou, int size){
        super();
        this.isTargetEnnemy = isTargetEnnemy;
        this.isCenterYou = isCenterYou;
        this.size = size;
    }


    //getter/setter
    public boolean isTargetEnnemy() {
        return isTargetEnnemy;
    }

    public boolean isCenterYou() {
        return isCenterYou;
    }

    public int getN() {
        return size;
    }
}
