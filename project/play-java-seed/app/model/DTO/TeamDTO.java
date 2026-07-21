package model.DTO;

import java.util.List;

public record TeamDTO(double streak, int health, int lvl, int exp, int gold, List<Long> shop, List<Long> items, List<Long> units) {
}
