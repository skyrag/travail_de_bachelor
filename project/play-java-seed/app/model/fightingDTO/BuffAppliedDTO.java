package model.fightingDTO;

public record BuffAppliedDTO(long t, int unit, String buff, double value) implements FightingEventDTO {}
