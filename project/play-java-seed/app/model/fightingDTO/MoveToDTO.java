package model.fightingDTO;

public record MoveToDTO(long t, int unit, double x, double y) implements FightingEventDTO {}
