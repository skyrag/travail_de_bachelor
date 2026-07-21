package model.service.fightingService;

import model.entities.Team;
import model.entities.effect.Effect;
import model.entities.effect.StatType;
import model.entities.unit.AbilityFragment;
import model.entities.unit.InstanceUnit;
import model.entities.unit.Item;
import model.entities.unit.Unit;
import model.utils.Tuple;

import java.util.ArrayList;
import java.util.List;

import static model.entities.effect.StatType.*;

public class ComponentUnit {

    private final long id;
    private int maxHealth;
    private int currentHealth;
    private int currentMana;
    private final int maxMana;
    private final int baseAttack;
    private int attackDamage;
    private int abilityPower;
    private int attackSpeed;
    private int attackTick;
    private int critChance;
    private int armor;
    private int magicResist;
    private int range;
    private final List<ActiveStatus> activeStatuses = new ArrayList<>();

    public ComponentUnit currentEnnemy;

    private Tuple currentPosition;
    private boolean alive;

    private final List<AbilityFragment> ability;

    private final Team team;

    public ComponentUnit(InstanceUnit unit) {
        id = unit.getId();
        maxHealth = Math.toIntExact(Math.round(unit.getUnit().getMaxHealth() * Math.pow(1.8, unit.getLvl() - 1)));
        currentHealth = maxHealth;
        currentMana = unit.getUnit().getStartingMana();
        maxMana = unit.getUnit().getMaxMana();
        baseAttack = Math.toIntExact(Math.round(unit.getUnit().getBaseAttack() * Math.pow(1.8, unit.getLvl() - 1)));
        attackDamage = 0;
        abilityPower = 0;
        attackSpeed = unit.getUnit().getAttackSpeed();
        critChance = 25;
        armor = unit.getUnit().getArmor();
        magicResist = unit.getUnit().getMagicResist();
        range = unit.getUnit().getRange();

        currentPosition = unit.getPos();
        alive = true;
        ability = unit.getUnit().getAbility();
        team = unit.getTeam();

        attackTick = attackSpeed;

        for (Item item: unit.getItems()){
            for (Effect effect: item.getEffects()){
                effect.applyTo(this, this, 0, 0);
            }
        }
    }

    public void increaseStat (StatType statType, int value){
        switch (statType) {
            case HEALTH -> maxHealth += value;
            case MANA -> currentMana += value;
            case ATTACKDAMAGE -> attackDamage += value;
            case ABILITYPOWER -> abilityPower += value;
            case ATTACKSPEED -> attackSpeed += value;
            case ARMOR -> armor += value;
            case MAGICRESIST -> magicResist += value;
            case RANGE -> range += value;
            default -> throw new IllegalArgumentException("Unknown type");
        }
    }

    public int getStat(StatType type){
        return switch (type) {
            case HEALTH -> maxHealth;
            case MANA -> currentMana;
            case ATTACKDAMAGE -> attackDamage;
            case ABILITYPOWER -> abilityPower;
            case ATTACKSPEED -> attackSpeed;
            case ARMOR -> armor;
            case MAGICRESIST -> magicResist;
            case RANGE -> range;
            default -> throw new IllegalArgumentException("Unknown type");
        };
    }

    public void applyStatus(StatusType type, int duration) {
        activeStatuses.stream()
                .filter(s -> s.getType() == type)
                .findFirst()
                .ifPresentOrElse(
                        s -> s.refresh(duration), // à ajouter dans ActiveStatus si tu veux refresh au lieu d'empiler
                        () -> activeStatuses.add(new ActiveStatus(type, duration))
                );
    }

    public boolean hasStatus(StatusType type) {
        return activeStatuses.stream().anyMatch(s -> s.getType() == type);
    }

    public void tickStatuses() {
        activeStatuses.removeIf(ActiveStatus::tick);
    }

    public int getDamage(){
        return Math.toIntExact(Math.round(baseAttack * (1 + attackDamage / 100.0)));
    }

    public boolean isAlive (){
        return alive;
    }

    public boolean canAttack(){
        if (attackTick == attackSpeed) return true;
        attackTick++;
        return false;
    }

    public boolean isFullMana() {
        return currentMana == maxMana;
    }

    public Team getTeam(){
        return team;
    }

    public Tuple getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Tuple currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getRange() {
        return range;
    }

    public int getArmor() {
        return armor;
    }

    public void addMana(int mana){
        currentMana = Math.min(maxMana, currentMana + mana);
    }

    public void resetMana(){
        currentMana = 0;
    }

    private void damage(int damage){
        if (!isAlive()) return;
        currentHealth -= damage;
        if (currentHealth <= 0){
            alive = false;
        }
    }

    public void heal(int heal){
        if (!isAlive()) return;
        currentHealth = Math.min(maxHealth, currentHealth + heal);
    }

    public int getCrit(){
        return critChance;
    }

    public long getId() {
        return id;
    }

    public List<AbilityFragment> getAbility(){
        return ability;
    }


    public int getMaxHealth() {
        return maxHealth;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public int getAttackSpeed() {
        return attackSpeed;
    }

    public int getMagicResist() {
        return magicResist;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public int getAbilityPower() {
        return abilityPower;
    }

    public int damageMagic(int trueDamage){
        int mitigatedDamage = Math.toIntExact(Math.round(trueDamage * (100.0 / (100 + magicResist))));
        damage(mitigatedDamage);
        return mitigatedDamage;
    }

    public int damagePhysic(int trueDamage){
        int mitigatedDamage = Math.toIntExact(Math.round(trueDamage * (100.0 / (100 + armor))));
        damage(mitigatedDamage);
        return mitigatedDamage;
    }
}
