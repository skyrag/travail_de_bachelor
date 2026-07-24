error id: file:///mnt/c/Users/Vivobook%20Pro/Documents/HEIG-VD/TB/travail_de_bachelor/project/play-java-seed/app/model/entities/Unit.java:_empty_/Entity#
file:///mnt/c/Users/Vivobook%20Pro/Documents/HEIG-VD/TB/travail_de_bachelor/project/play-java-seed/app/model/entities/Unit.java
empty definition using pc, found symbol in pc: _empty_/Entity#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 61
uri: file:///mnt/c/Users/Vivobook%20Pro/Documents/HEIG-VD/TB/travail_de_bachelor/project/play-java-seed/app/model/entities/Unit.java
text:
```scala
package model.entities;

import jakarta.persistence.*;

@@@Entity
@Table(name = "unit")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    public String name;

    // regarder comment on stocke les icon et pixelart

    public int cost;

    public String ability_name;

    public String ability_description;

    public int maxHealth;

    public int startingMana;

    public int maxMana;

    public int baseAttack;

    public float attackSpeed;

    public int armor;

    public int magicResist;

    public int range;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public String getAbility_name() {
        return ability_name;
    }

    public void setAbility_name(String ability_name) {
        this.ability_name = ability_name;
    }

    public String getAbility_description() {
        return ability_description;
    }

    public void setAbility_description(String ability_description) {
        this.ability_description = ability_description;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getStartingMana() {
        return startingMana;
    }

    public void setStartingMana(int startingMana) {
        this.startingMana = startingMana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public void setBaseAttack(int baseAttack) {
        this.baseAttack = baseAttack;
    }

    public float getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(float attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public int getArmor() {
        return armor;
    }

    public void setArmor(int armor) {
        this.armor = armor;
    }

    public int getMagicResist() {
        return magicResist;
    }

    public void setMagicResist(int magicResist) {
        this.magicResist = magicResist;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Entity#