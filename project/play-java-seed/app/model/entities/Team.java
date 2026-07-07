package model.entities;

import jakarta.persistence.*;
import model.entities.game.Game;
import model.entities.unit.InstanceUnit;
import model.entities.unit.Unit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Integer winstreak;

    @Column(nullable = false)
    private Integer health;

    @Column(nullable = false)
    private Integer lvl;

    @Column(nullable = false)
    private Integer gold;

    @ManyToMany
    @JoinTable(
            name = "teams_shop",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "unit_id")
    )
    private List<Unit> shop = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstanceUnit> units = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    protected Team() {

    }

    public Team(User user, Game game, int gold) {
        this.user = user;
        this.game = game;
        this.gold = gold;
        this.winstreak = 0;
        this.health = 100;
        this.lvl = 1;
    }



    //getter/setter
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Game getGame() {
        return game;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getWinstreak() {
        return winstreak;
    }

    public void setWinstreak(Integer winstreak) {
        this.winstreak = winstreak;
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
}
