package model.fightingDTO;

public record UnitDTO(long id,
                      String name,
                      int cost,
                      String rarity,
                      String abilityName,
                      String abilityDescription,
                      int maxHealth,
                      int maxMana,
                      int startingMana,
                      int baseAttack,
                      double attackSpeed,
                      int armor,
                      int magicResist,
                      int range) {
}

