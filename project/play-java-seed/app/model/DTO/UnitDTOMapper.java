package model.DTO;

import model.entities.unit.Unit;
import model.service.fightingService.ComponentUnit;

public class UnitDTOMapper {

    public static UnitDTO unitToDTO (Unit unit, String sprite){
        return new UnitDTO(unit.getId(),
                sprite,
                unit.getName(),
                unit.getCost(),
                unit.getRarity().toString(),
                unit.getAbilityName(),
                unit.getAbilityDescription(),
                unit.getMaxHealth(),
                unit.getMaxMana(),
                unit.getStartingMana(),
                unit.getBaseAttack(),
                0,
                0,
                unit.getAttackSpeed(),
                unit.getArmor(),
                unit.getMagicResist(),
                unit.getRange());
    }

    public static ComponentUnitDTO componentToDTO (ComponentUnit unit){
        return new ComponentUnitDTO(unit.getId(),
                unit.getTeam().getId(),
                unit.getName(),
                unit.getMaxHealth(),
                unit.getMaxMana(),
                unit.getCurrentMana(),
                unit.getBaseAttack(),
                unit.getAttackDamage(),
                unit.getAbilityPower(),
                unit.getAttackSpeed(),
                unit.getArmor(),
                unit.getMagicResist(),
                unit.getRange(),
                unit.getCurrentPosition().x(),
                unit.getCurrentPosition().y());
    }
}
