package model.fightingDTO;

public record TargetAcquiredDTO(long t, int unit, int target) implements FightingEventDTO {
}
