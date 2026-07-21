package model.DTO.fighting;

import java.util.List;

public record CombatEndDTO(long t, long winner) implements FightingEventDTO {}
