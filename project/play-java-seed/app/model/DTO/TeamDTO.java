package model.DTO;

import java.util.List;

public record TeamDTO(long id,String username, double streak, int health, int lvl, int exp, int gold, List<Long> shop, List<Long> items, List<TeamsUnitDTO> units) {
}
