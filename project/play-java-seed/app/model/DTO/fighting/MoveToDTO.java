package model.DTO.fighting;

public record MoveToDTO(long t, long unitId, int x, int y) implements FightingEventDTO {}
