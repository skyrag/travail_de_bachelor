package model.fightingDTO;

import java.util.List;

public record CombatEndDTO(long t, List<Integer> survivors) implements FightingEventDTO {}
