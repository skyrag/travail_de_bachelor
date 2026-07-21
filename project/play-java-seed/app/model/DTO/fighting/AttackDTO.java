package model.DTO.fighting;

public record AttackDTO(long t, long unit, long target, int damage, boolean crit) implements FightingEventDTO {

}
