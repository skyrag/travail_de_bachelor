package model.fightingDTO;

public record AttackDTO(long t, int unit, int target, int damage, boolean crit) implements FightingEventDTO {

}
