package model.DTO.fighting;

import model.DTO.ComponentUnitDTO;
import model.DTO.UnitDTO;
import model.service.fightingService.ComponentUnit;

import java.util.List;

public record FightingResultDTO(List<ComponentUnitDTO> initialState, List<FightingEventDTO> events, List<ComponentUnitDTO> finalState, long idTeamA, int pvLostTeamA, long idTeamB, int pvLostTeamB) {
}
