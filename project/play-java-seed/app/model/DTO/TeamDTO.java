package model.DTO;

import java.util.List;

public record TeamDTO(long id, double streak, int health, int lvl, int exp, int gold, List<Long> shop, List<Long> items, List<TeamsUnitDTO> units) {
}
