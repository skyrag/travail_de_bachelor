package model.entities;

import jakarta.persistence.*;
import model.entities.event.*;
import model.entities.event.unit.BuyUnitEvent;
import model.entities.event.unit.ChangingPosEvent;
import model.entities.event.unit.ChangingUnitObjectEvent;
import model.entities.event.unit.SellUnitEvent;
import model.entities.game.Game;
import model.entities.unit.InstanceUnit;
import model.entities.unit.Item;
import model.entities.unit.Unit;
import model.utils.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static model.utils.Constante.*;

/**
 * Represents a player's team within a Game owned by a User.
 * <p>
 * A Team tracks the player's current state during the game:
 * their rank among opponents, health, winstreak, level, gold, and the
 * list of units currently available in their shop.
 */
@Entity
@Table(name = "team")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private Double streak;

    @Column(nullable = false)
    private Integer health;

    @Column(nullable = false)
    private Integer lvl;

    @Column(nullable = false)
    private Integer exp;

    @Column(nullable = false)
    private Integer gold;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Round> rounds = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "teams_shop",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "unit_id")
    )
    private List<Unit> shop = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstanceUnit> units = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "teams_object",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "object_id")
    )
    private List<Item> items = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Transient
    private Random seed;

    @Transient
    private boolean dead;

    protected Team() {

    }

    public Team(User user, Game game, int gold) {
        this.user = user;
        this.game = game;
        this.gold = gold;
        this.rank = 1;
        this.streak = 0.0;
        this.health = 100;
        this.lvl = 1;
    }

    public Tuple firstEmptySpace(){
        List<Tuple> positions = new ArrayList<>();

        int benchPlaceTaken = 0;
        int boardPlaceTaken = 0;

        for(InstanceUnit unit : getUnits()) {
            model.utils.Tuple pos = unit.getPos();
            positions.add(unit.getPos());
            if (pos.y() == 0){
                benchPlaceTaken++;
            } else {
                boardPlaceTaken++;
            }
        }
        if (benchPlaceTaken == MAXBENCHSIZE && boardPlaceTaken == getLvl()){
            return null;
        }
        positions.sort(Comparator.comparingInt(Tuple::y)
                .thenComparingInt(Tuple::x));
        Tuple newPos = new Tuple(0,0);
        for (Tuple pos : positions){
            if (pos.y() != newPos.y() || pos.x() != newPos.x()){
                return newPos;
            }
            if (newPos.y() == 0 && newPos.x() + 1 >= MAXBENCHSIZE){
                newPos = new Tuple(0, 1);
            } else {
                if (newPos.x() + 1 >= MAXXBOARD){
                    newPos = new Tuple(0, newPos.y() + 1);
                } else {
                    newPos = new Tuple(newPos.x() + 1, newPos.y());
                }
            }
        }
        return newPos;
    }

    public InstanceUnit isOccupied (Tuple position){
        for (InstanceUnit unit : units){
            if (unit.getPos().equals(position)){
                return unit;
            }
        }
        return null;
    }

    public boolean isFull (){
        int boardPlaceTaken = 0;

        for (InstanceUnit unit: units){
            if (unit.getPos().y() > 0){
                boardPlaceTaken++;
            }
        }
        return boardPlaceTaken == lvl;
    }

    public void addExp(int exp, int maxExp) {
        int newExp = this.exp + exp;
        if (newExp >= maxExp){
            newExp -= maxExp;
            this.exp = newExp;
            this.lvl++;
        } else {
            this.exp+= newExp;
        }
    }

    public void lostFight(int health){
        streak = streak < 0 ? streak - 1 : -1;
        this.health -= health;
        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        currentRound.getEvents().add(new LosingHealthEvent(step, currentRound, health));
    }

    public void wonFight(){
        streak = streak > 0 ? streak + 1 : 1;
        gold += WINNINGGAINS;
    }

    public void newRound(){
        rounds.add(new Round(rounds.getLast().getRoundNumber() + 1,this));
        gold += ENDOFROUNDGOLD;
        exp += ENDOFROUNDEXP;

        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        currentRound.getEvents().add(new ChangingGoldEvent(step, currentRound, ENDOFROUNDGOLD));
    }

    public boolean canBuyExp(int maxExp){
        if (gold < EXPCOST && lvl != 10){
            return false;
        }
        addExp(AMOUNTEXPBOUGHT, maxExp);

        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        currentRound.getEvents().add(new LevelingEvent(step, currentRound, AMOUNTEXPBOUGHT));
        return true;
    }

    public boolean canReroll(List<Unit> shop){
        if (gold < REROLLCOST){
            return false;
        }

        gold -= REROLLCOST;
        this.shop = shop;

        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        currentRound.getEvents().add(new ChangingShopEvent(step, currentRound, shop));
        return true;
    }

    public boolean canAddItemToUnit(long itemId, long unitId){
        Item item = getItemById(itemId);
        InstanceUnit unit = getUnitById(unitId);
        if (item == null || unit == null || unit.getItems().size() >= MAXNBITEMHOLDED) {
            return false;
        }

        unit.getItems().add(item);
        items.remove(item);


        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        currentRound.getEvents().add(new ChangingUnitObjectEvent(unit, step, currentRound, unit.getItems()));
        return true;
    }

    public boolean canMoveUnit(Tuple newPosition, long unitId){

        boolean isCorrectPos = newPosition.x() < MAXXTEAMBOARD && newPosition.y() >= 0 && newPosition.y() < MAXYBOARD &&  newPosition.x() >= 0;
        InstanceUnit unit = getUnitById(unitId);

        if (unit == null || !isCorrectPos) {
            return false;
        }

        InstanceUnit unitToSwap = isOccupied(newPosition);
        if (unitToSwap != null){
            unitToSwap.setPos(unit.getPos());
            unit.setPos(newPosition);
        } else {
            if (newPosition.y() == 0){
                unit.setPos(newPosition);
            } else {
                if (!isFull()){
                    unit.setPos(newPosition);
                } else {
                    return false;
                }
            }
        }


        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        currentRound.getEvents().add(new ChangingPosEvent(unit, step, currentRound, newPosition));
        return true;
    }

    public boolean canSellUnit(long unitId){
        InstanceUnit unit = getUnitById(unitId);
        if (unit == null){
            return false;
        }
        units.remove(unit);
        gold += (int) (unit.getUnit().getCost() * Math.pow(3, unit.getLvl()) - 1);
        items.addAll(unit.getItems());

        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        currentRound.getEvents().add(new SellUnitEvent(unit, step, currentRound));
        return true;
    }

    public boolean canBuyUnit (Unit unit){
        Tuple newPos = firstEmptySpace();
        if (!shop.contains(unit) || gold < unit.getCost() || newPos == null){
            return false;
        }
        gold -= unit.getCost();
        shop.remove(unit);
        InstanceUnit instance = new InstanceUnit(1, newPos, unit, this);
        addUnit(instance);

        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        currentRound.getEvents().add(new BuyUnitEvent(instance, step, currentRound));
        return true;
    }


    //getter/setter

    public void addUnit(InstanceUnit unit) {
        units.add(unit);
    }

    public Item getItemById (long id) {
        for (Item item: items){
            if (item.getId() == id){
                return item;
            }
        }
        return null;
    }
    public InstanceUnit getUnitById(long id){
        for (InstanceUnit unit : units){
            if (unit.getId() == id) {
                return unit;
            }
        }
        return null;
    }

    public void die(){
        dead = true;
    }

    public boolean isDead(){
        return dead;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public double getStreak() {
        return streak;
    }

    public void setStreak(double winstreak) {
        this.streak = winstreak;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Integer getLvl() {
        return lvl;
    }

    public void setLvl(Integer lvl) {
        this.lvl = lvl;
    }

    public Integer getGold() {
        return gold;
    }

    public void setGold(Integer gold) {
        this.gold = gold;
    }

    public List<Unit> getShop() {
        return shop;
    }

    public void setShop(List<Unit> shop) {
        this.shop = shop;
    }

    public List<InstanceUnit> getUnits() {
        return units;
    }

    public void setUnits(List<InstanceUnit> units) {
        this.units = units;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setSeed(long seed){
        this.seed = new Random(seed);
    }

    public Random getSeed(){
        return seed;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp){
        this.exp = exp;
    }

    public List<Round> getRounds(){
        return rounds;
    }
}
