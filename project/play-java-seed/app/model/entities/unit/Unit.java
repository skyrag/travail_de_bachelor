package model.entities.unit;

import jakarta.persistence.*;
import model.entities.game.Rarity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a playable unit (champion/character) in the game, tied
 * to a specific patchVersion of the game's balancing data.
 * <p>
 * A Unit holds its base stats (health, mana, attack, armor,
 * magic resist, range...), its cost, and the definition of its
 * ability (name, description, and the ordered AbilityFragment
 * list describing how the ability behaves.
 */
@Entity
@Table(name = "unit")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patch_version", nullable = false, length = 20)
    private String patchVersion;

    @Column(name = "sprite_key",nullable = false)
    private long spriteKey;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false)
    private Integer cost;

    @Column(name = "ability_name", nullable = false, length = 30)
    private String abilityName;

    @Column(name = "ability_description", nullable = false, columnDefinition = "TEXT")
    private String abilityDescription;

    /**
     * The unit's ability, broken down into a list of fragments so that we can have different effect
     * that may affect different groups of poeple
     */
    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AbilityFragment> ability = new ArrayList<>();

    @Column(name = "max_health", nullable = false)
    private Integer maxHealth;

    /**
     * Mana the unit starts a game/round with (before regeneration or
     * ability casts).
     */
    @Column(name = "starting_mana", nullable = false)
    private Integer startingMana;

    @Column(name = "max_mana", nullable = false)
    private Integer maxMana;

    @Column(name = "base_attack", nullable = false)
    private Integer baseAttack;

    @Column(name = "attack_speed", nullable = false)
    private Integer attackSpeed;

    @Column(nullable = false)
    private Integer armor;

    @Column(name = "magic_resist", nullable = false)
    private Integer magicResist;

    @Column(nullable = false)
    private Integer range;

    @Column(nullable = false)
    private Rarity rarity;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    protected Unit(){

    }

    public Unit(String patchVersion,
                String name,
                int cost,
                String abilityName,
                String abilityDescription,
                List<AbilityFragment> ability,
                int maxHealth,
                int startingMana,
                int maxMana,
                int baseAttack,
                int attackSpeed,
                int armor,
                int magicResist,
                int range,
                Rarity rarity){
        this.patchVersion = patchVersion;
        this.name = name;
        this.cost = cost;
        this.abilityName = abilityName;
        this.abilityDescription = abilityDescription;
        this.ability = ability;
        this.maxHealth = maxHealth;
        this.startingMana = startingMana;
        this.maxMana = maxMana;
        this.baseAttack = baseAttack;
        this.attackSpeed = attackSpeed;
        this.armor = armor;
        this.magicResist = magicResist;
        this.range= range;
        this.rarity = rarity;
    }


    //getter/setter
    public Long getId() {
        return id;
    }

    public String getPatchVersion() {
        return patchVersion;
    }

    public String getName() {
        return name;
    }

    public Integer getCost() {
        return cost;
    }

    public String getAbilityName() {
        return abilityName;
    }

    public String getAbilityDescription() {
        return abilityDescription;
    }

    public Integer getMaxHealth() {
        return maxHealth;
    }

    public Integer getStartingMana() {
        return startingMana;
    }

    public Integer getMaxMana() {
        return maxMana;
    }

    public Integer getBaseAttack() {
        return baseAttack;
    }

    public Integer getAttackSpeed() {
        return attackSpeed;
    }

    public Integer getArmor() {
        return armor;
    }

    public Integer getMagicResist() {
        return magicResist;
    }

    public Integer getRange() {
        return range;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<AbilityFragment> getAbility() {
        return ability;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public long getSpriteKey() {
        return spriteKey;
    }
}
