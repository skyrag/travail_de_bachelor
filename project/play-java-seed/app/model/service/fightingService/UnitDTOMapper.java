package model.service.fightingService;

import model.entities.unit.Unit;
import model.fightingDTO.UnitDTO;

public class UnitDTOMapper {
    //TODO

    public static UnitDTO unitToDTO (Unit unit){
        return new UnitDTO(unit.getId(),
                unit.getName(),
                unit.getCost(),
                unit.getRarity().toString(),
                unit.getAbilityName(),
                unit.getAbilityDescription(),
                unit.getMaxHealth(),
                unit.getMaxMana(),
                unit.getStartingMana(),
                unit.getBaseAttack(),
                unit.getAttackSpeed(),
                unit.getArmor(),
                unit.getMagicResist(),
                unit.getRange());
    }
}
