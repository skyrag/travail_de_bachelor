package model.service.fightingService.system;

import model.DTO.fighting.FightingEventDTO;
import model.service.fightingService.ComponentUnit;
import model.service.fightingService.FightingContext;

import java.util.ArrayList;
import java.util.List;

public class StatusSystem implements System{

    @Override
    public List<FightingEventDTO> update(FightingContext context) {
        for (ComponentUnit unit : context.getAliveUnits()){
            unit.tickStatuses();
        }
        return new ArrayList<>();
    }
}
