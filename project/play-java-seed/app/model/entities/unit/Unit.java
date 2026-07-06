package model.entities.unit;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "unit")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patch_version", nullable = false, length = 20)
    private String patchVersion;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false)
    private Integer cost;

    @Column(name = "ability_name", nullable = false, length = 30)
    private String abilityName;

    @Column(name = "ability_description", nullable = false, columnDefinition = "TEXT")
    private String abilityDescription;

    @Column(name = "max_health", nullable = false)
    private Integer maxHealth;

    @Column(name = "starting_mana", nullable = false)
    private Integer startingMana;

    @Column(name = "max_mana", nullable = false)
    private Integer maxMana;

    @Column(name = "base_attack", nullable = false)
    private Integer baseAttack;

    @Column(name = "attack_speed", nullable = false)
    private Float attackSpeed;

    @Column(nullable = false)
    private Integer armor;

    @Column(name = "magic_resist", nullable = false)
    private Integer magicResist;

    @Column(nullable = false)
    private Integer range;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AbilityFragment> ability = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatchVersion() {
        return patchVersion;
    }

    public void setPatchVersion(String patchVersion) {
        this.patchVersion = patchVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public String getAbilityName() {
        return abilityName;
    }

    public void setAbilityName(String abilityName) {
        this.abilityName = abilityName;
    }

    public String getAbilityDescription() {
        return abilityDescription;
    }

    public void setAbilityDescription(String abilityDescription) {
        this.abilityDescription = abilityDescription;
    }

    public Integer getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(Integer maxHealth) {
        this.maxHealth = maxHealth;
    }

    public Integer getStartingMana() {
        return startingMana;
    }

    public void setStartingMana(Integer startingMana) {
        this.startingMana = startingMana;
    }

    public Integer getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(Integer maxMana) {
        this.maxMana = maxMana;
    }

    public Integer getBaseAttack() {
        return baseAttack;
    }

    public void setBaseAttack(Integer baseAttack) {
        this.baseAttack = baseAttack;
    }

    public Float getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(Float attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public Integer getArmor() {
        return armor;
    }

    public void setArmor(Integer armor) {
        this.armor = armor;
    }

    public Integer getMagicResist() {
        return magicResist;
    }

    public void setMagicResist(Integer magicResist) {
        this.magicResist = magicResist;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer range) {
        this.range = range;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<AbilityFragment> getAbility() {
        return ability;
    }

    public void setAbility(List<AbilityFragment> ability) {
        this.ability = ability;
    }
}
