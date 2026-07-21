package model.entities.unit.strategie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import model.service.fightingService.ComponentUnit;
import model.service.fightingService.FightingContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    @Override
    public List<ComponentUnit> findTarget(ComponentUnit caster, FightingContext context) {
        List<ComponentUnit> targets = new ArrayList<>();
        for (ComponentUnit unit: context.getGroup(caster, isTargetEnnemy).stream().filter(ComponentUnit::isAlive).toList()){
            if (caster.getCurrentPosition().distanceFrom(unit.getCurrentPosition()) <  Math.sqrt(2)){
                targets.add(unit);
            }
        }
        context.getGroup(caster, isTargetEnnemy).stream().filter(ComponentUnit::isAlive).sorted(Comparator.comparingDouble(
                unit -> caster.getCurrentPosition().distanceFrom(unit.getCurrentPosition())
        )).limit(n).toList();
        return targets;
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
