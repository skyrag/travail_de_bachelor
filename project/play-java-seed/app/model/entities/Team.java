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
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.*;

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
    @OrderColumn(name = "slot")
    private List<Unit> shop = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @SQLRestriction("sold = false")
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
        this.exp = 0;
    }

    private static final Tuple BENCH = new Tuple(-1, -1);

    public InstanceUnit isOccupied (Tuple position){
        for (InstanceUnit unit : units){
            if (unit.getPos().equals(position)){
                return unit;
            }
        }
        return null;
    }

    public static boolean isBench(Tuple pos) {
        return pos.x() == -1 && pos.y() == -1;
    }

    private int benchCount() {
        int n = 0;
        for (InstanceUnit u : units) if (isBench(u.getPos())) n++;
        return n;
    }

    private int boardCount() {
        return units.size() - benchCount();
    }

    public Tuple firstEmptySpace() {
        if (benchCount() < MAXBENCHSIZE) {
            return BENCH;
        }
        if (boardCount() < lvl) {
            for (int y = 0; y < MAXYBOARD; y++) {
                for (int x = 0; x < MAXXTEAMBOARD; x++) {
                    Tuple p = new Tuple(x, y);
                    if (isOccupied(p) == null) return p;
                }
            }
        }
        return null;
    }

    public boolean isFull() {
        return boardCount() >= lvl;
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


    public void newRound() {
        int number = rounds.isEmpty() ? 1 : rounds.getLast().getRoundNumber() + 1;
        Round round = new Round(number, this);
        rounds.add(round);
        if (number > 1) {                       // pas de gold/exp au tout premier round, si tu le souhaites
            gold += ENDOFROUNDGOLD;
            exp += ENDOFROUNDEXP;
        }
        round.getEvents().add(new ChangingGoldEvent(0, round, number > 1 ? ENDOFROUNDGOLD : 0));
    }

    public Event canBuyExp(int maxExp){
        if (gold < EXPCOST || lvl >= 10){
            return null;
        }

        this.gold -= EXPCOST;
        addExp(AMOUNTEXPBOUGHT, maxExp);

        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        Event event = new LevelingEvent(step, currentRound, AMOUNTEXPBOUGHT);
        currentRound.getEvents().add(event);
        return event;
    }

    public Event Reroll(List<Unit> shop){
        if (gold < REROLLCOST){
            return null;
        }

        gold -= REROLLCOST;
        this.shop = shop;

        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        Event event = new ChangingShopEvent(step, currentRound, new ArrayList<>(shop));
        currentRound.getEvents().add(event);
        return event;
    }

    public Team.Result addItemToUnit(String itemName, long unitId){
        Item item = getItemByName(itemName);
        InstanceUnit unit = getUnitById(unitId);

        if (item == null || unit == null || unit.getItems().size() >= MAXNBITEMHOLDED) {
            return null;
        }

        unit.getItems().add(item);
        items.remove(item);


        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        Event event = new ChangingUnitObjectEvent(unit, step, currentRound, unit.getItems());
        currentRound.getEvents().add(event);
        return new Result(unit, event);
    }

    public Result moveUnit(Tuple newPosition, long unitId) {
        InstanceUnit unit = getUnitById(unitId);
        if (unit == null) return null;

        boolean fromBench = isBench(unit.getPos());

        if (isBench(newPosition)) {
            // vers le banc : refuser seulement si le banc est plein ET que l'unité n'y est pas déjà
            if (!fromBench && benchCount() >= MAXBENCHSIZE) return null;
        } else {
            if (!correctPos(newPosition)) return null;          // case valide du plateau
            if (isOccupied(newPosition) != null) return null;   // case déjà prise
            if (fromBench && isFull()) return null;             // le plateau n'accepte plus de nouvelle unité
        }

        unit.setPos(newPosition);

        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        Event event = new ChangingPosEvent(unit, step, currentRound, newPosition);
        currentRound.getEvents().add(event);
        return new Result(unit, event);
    }

    private boolean correctPos(Tuple tuple){
        return tuple.x() < MAXXTEAMBOARD && tuple.y() >= 0 && tuple.y() < MAXYBOARD &&  tuple.x() >= 0;
    }

    public record Result(InstanceUnit instance, Event event) {}

    public Result buyUnit(int slot) {
        Unit unit = shop.get(slot);
        System.out.println(unit.getName());

        Tuple newPos = firstEmptySpace();
        if (unit == null || gold < unit.getCost() || newPos == null) return null;

        gold -= unit.getCost();
        shop.set(slot, null);
        InstanceUnit instance = new InstanceUnit(1, newPos, unit, this);
        addUnit(instance);

        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        BuyUnitEvent event = new BuyUnitEvent(instance, step, currentRound);
        currentRound.getEvents().add(event);
        return new Result(instance, event);
    }

    public Result sellUnit(long id) {
        InstanceUnit unit = getUnitById(id);
        if (unit == null){
            return null;
        }
        units.remove(unit);
        gold += (int) (unit.getUnit().getCost() * Math.pow(3, unit.getLvl() - 1));
        items.addAll(unit.getItems());
        unit.getItems().clear();

        Round currentRound = rounds.getLast();
        int step = currentRound.getEvents().getLast().getStep() + 1;
        SellUnitEvent event = new SellUnitEvent(unit, step, currentRound);
        currentRound.getEvents().add(event);
        return new Result(unit, event);

    }


    //getter/setter

    public void addUnit(InstanceUnit unit) {
        units.add(unit);
    }

    public Item getItemByName(String name) {
        for (Item item: items){
            if (Objects.equals(item.getName(), name)){
                return item;
            }
        }
        return null;
    }

    public void setItems(List<Item> items){
        this.items = new ArrayList<>(items);
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

    public void setRandom(Random seed) {
        this.seed = seed;
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
