package model.service.fightingService.system;

import model.DTO.fighting.AttackDTO;
import model.DTO.fighting.DeathDTO;
import model.DTO.fighting.FightingEventDTO;
import model.DTO.fighting.MoveToDTO;
import model.entities.effect.Effect;
import model.entities.unit.AbilityFragment;
import model.service.fightingService.ComponentUnit;
import model.service.fightingService.FightingContext;
import model.utils.Tuple;

import java.util.ArrayList;
import java.util.List;

public class ActionSystem implements System{

    private static int MANAPERATTACK = 10;
    private int abilityUUID = 0;

    @Override
    public List<FightingEventDTO> update(FightingContext context) {
        List<FightingEventDTO> events = new ArrayList<>();
        for (ComponentUnit unit : context.getAliveUnits()){
            if (unit.isFullMana()){
                events.addAll(castAbility(unit, context));
            } else {
                events.addAll(basicAttack(unit, context));
            }
        }
        return events;
    }

    private List<FightingEventDTO> basicAttack(ComponentUnit unit, FightingContext context){
        List<FightingEventDTO> res = new ArrayList<>();
        ComponentUnit enemy = context.getCurrentTarget(unit);
        if(unit.getCurrentPosition().distanceFrom(enemy.getCurrentPosition()) <= unit.getRange() * Math.sqrt(2.0) && unit.canAttack()){ //to be able to hit it, the distance between them should be less or equal to range * sqrt(2)
            int trueDamage = unit.getDamage();
            boolean crit = false;

            if(context.randomInt(100) < unit.getCrit()){
                trueDamage += trueDamage;
                crit = true;
            }

            int mitigatedDamage = enemy.damagePhysic(trueDamage);
            if (!enemy.isAlive()) res.add(new DeathDTO(context.getTick(), enemy.getId()));
            unit.addMana(MANAPERATTACK);
            res.add(new AttackDTO(context.getTick(), unit.getId(), enemy.getId(), mitigatedDamage, crit));
        } else {
            Tuple move = context.getNextMove(unit);
            context.move(unit.getCurrentPosition(), move);
            unit.setCurrentPosition(move);
            res.add(new MoveToDTO(context.getTick(), unit.getId(), move.x(), move.y()));
        }
        return res;
    }

    private List<FightingEventDTO> castAbility(ComponentUnit caster, FightingContext context){
        List<FightingEventDTO> events = new ArrayList<>();
        for (AbilityFragment ability: caster.getAbility()){
            for (ComponentUnit target: ability.getStrategie().findTarget(caster, context)){
                for (Effect effect : ability.getEffects()){
                    events.add(effect.applyTo(target, caster,context.getTick(), abilityUUID));
                }
                if (!target.isAlive()){
                    events.add(new DeathDTO(context.getTick(), target.getId()));
                }
            }
        }
        abilityUUID++;
        caster.resetMana();
        return events;
    }
}
