package model.DTO;

public record UnitDTO(long id,
                      String sprite,
                      String name,
                      int cost,
                      String rarity,
                      String abilityName,
                      String abilityDescription,
                      int maxHealth,
                      int maxMana,
                      int startingMana,
                      int baseAttack,
                      int attackDamage,
                      int abilityPower,
                      int attackSpeed,
                      int armor,
                      int magicResist,
                      int range) {
}

