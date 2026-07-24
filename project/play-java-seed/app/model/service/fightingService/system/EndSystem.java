package model.service.fightingService.system;

import model.DTO.fighting.CombatEndDTO;
import model.DTO.fighting.FightingEventDTO;
import model.service.fightingService.ComponentUnit;
import model.service.fightingService.FightingContext;

import java.util.ArrayList;
import java.util.List;

public class EndSystem implements System{

    private Long winner;
    private boolean isFinished;

    @Override
    public List<FightingEventDTO> update(FightingContext context) {
        List<ComponentUnit> teamA;
        List<ComponentUnit> teamB;
        List<FightingEventDTO> res = new ArrayList<>();

        teamA = context.getAllies(context.getAliveUnits().getFirst()).stream().filter(ComponentUnit::isAlive).toList();
        teamB = context.getEnemies(context.getAliveUnits().getFirst()).stream().filter(ComponentUnit::isAlive).toList();

        if(teamA.isEmpty() && teamB.isEmpty()){
            winner = null;
            res.add(new CombatEndDTO(context.getTick(), -1L));
            isFinished = true;
            return res;
        }

        if (teamA.isEmpty()){
            winner = teamB.getFirst().getTeam().getId();
            res.add(new CombatEndDTO(context.getTick(), winner));
            isFinished = true;
            return res;
        }
        if (teamB.isEmpty()) {
            winner = teamA.getFirst().getTeam().getId();
            res.add(new CombatEndDTO(context.getTick(), winner));
            isFinished = true;
            return res;
        }
        return res;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public long getWinner() {
        return winner;
    }
}
