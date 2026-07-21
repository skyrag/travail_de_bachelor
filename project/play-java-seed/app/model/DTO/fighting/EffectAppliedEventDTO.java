package model.DTO.fighting;

public record EffectAppliedEventDTO(long t, long abilityId, long targetId, long casterId, String effectType, int value) implements FightingEventDTO {
}
