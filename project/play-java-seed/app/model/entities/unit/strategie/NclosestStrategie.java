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
    private Boolean isTargetEnnemy;

    @Column(nullable = false)
    private Integer n;

    protected NclosestStrategie(){
        super();
    }

    public NclosestStrategie(boolean isTargetEnnemy, int n){
        this.isTargetEnnemy= isTargetEnnemy;
        this.n = n;
    }


    //getter/setter
    public boolean isTargetEnnemy() {
        return isTargetEnnemy;
    }

    public int getN() {
        return n;
    }
}
