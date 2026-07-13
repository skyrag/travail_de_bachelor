package model.fightingDTO;

import model.service.fightingService.ComponentUnit;

import java.util.List;

public record FightingResultDTO(List<UnitDTO> initialState, List<FightingEventDTO> events, List<UnitDTO> finalState, long winnerId) {
}
