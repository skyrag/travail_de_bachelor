package model.service.fightingService.system;

import model.DTO.fighting.FightingEventDTO;
import model.service.fightingService.FightingContext;

import java.util.List;

public interface System {
    public List<FightingEventDTO> update(FightingContext context);
}
