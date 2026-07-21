package model.entities.unit.strategie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import model.service.fightingService.ComponentUnit;
import model.service.fightingService.FightingContext;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<ComponentUnit> findTarget(ComponentUnit caster, FightingContext context){
        ComponentUnit center;
        if(isCenterYou) {
            center = caster;
        } else {
            if (isTargetEnnemy){
                center = context.getCurrentTarget(caster);
            } else {
                center = context.getClosestAlly(caster);
            }
        }

        List<ComponentUnit> targets = new ArrayList<>();
        if (!isCenterYou) targets.add(center);
        for (ComponentUnit unit: context.getGroup(caster, isTargetEnnemy).stream().filter(ComponentUnit::isAlive).toList()){
            if (center.getCurrentPosition().distanceFrom(unit.getCurrentPosition()) < size * Math.sqrt(2)){
                targets.add(unit);
            }
        }
        return targets;
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
