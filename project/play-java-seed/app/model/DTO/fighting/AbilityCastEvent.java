package model.DTO.fighting;

public record AbilityCastEvent(long t, long abilityId, long casterId) implements FightingEventDTO {
}
