package model.entities.game;

import jakarta.persistence.*;
import model.entities.Team;
import model.entities.unit.InstanceUnit;
import model.entities.unit.Unit;
import org.apache.pekko.actor.typed.javadsl.Behaviors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Represents a single match/game session, tied to a specific
 * patchVersion of the game's balancing data.
 * <p>
 * A Game is deterministic given its seed (used for
 * reproducible randomness, e.g. shop rolls), and holds the list of
 * Teams competing in it as well as the Pools of units
 * available to draw from.
 */
@Entity
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patch_version", nullable = false, length = 20)
    private String patchVersion;

    @Column(nullable = false)
    private Long seed;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pool> pools = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Team> teams = new ArrayList<>();

    @Transient
    private Team lastDied;

    protected Game() {

    }

    public Game(String patchVersion, Long seed){
        this.patchVersion = patchVersion;
        this.seed = seed;
    }

    public List<Team> stillAlive(){
        List<Team> res= new ArrayList<>();
        for (Team team: teams){
            if (team.isDead()) {
                res.add(team);
            }
        }
        return res;
    }

    public void adjustRankings(){
        List<Team> remaining = stillAlive();
        remaining.sort(Comparator.comparingInt(Team::getHealth));
        for (int i = 0; i < remaining.size(); i++){
            teams.get(i).setRank(i + 1);
        }
    }

    public boolean canAddUnitToPool(long unitId){
        PoolEntry entry = getPool(unitId);
        if (entry == null){
            return false;
        }
        entry.increment();
        return true;
    }

    public boolean canRemoveUnitToPool(long unitId) {
        PoolEntry entry = getPool(unitId);
        if (entry == null){
            return false;
        }
        if (entry.getNumber() - 1 > 0){
            entry.decrement();
        }
        return true;
    }

    public void died(Team team){
        adjustRankings();
        team.die();
        lastDied = team;
    }

    public Unit randomUnitFromPool(Pool pool, Random rand){
        int total = pool.getEntries().stream().mapToInt(PoolEntry::getNumber).sum();
        int r = rand.nextInt(total);
        int cumulative = 0;
        for (PoolEntry entry : pool.getEntries()) {
            cumulative += entry.getNumber();
            if (r < cumulative) {
                return entry.getUnit();
            }
        }
        throw new IllegalStateException("problème de cohérence dans le pool :" + pool.getPoolsRarity());
    }

    //getter/setter

    public Team getLastDied(){
        return lastDied;
    }

    public Team getTeam(long userId) {
        for (Team team: teams){
            if (team.getUser().getId() == userId){
                return team;
            }
        }
        return null;
    }

    public PoolEntry getPool(long unitId){
        for(Pool pool : pools){
            for (PoolEntry entry : pool.getEntries()){
                if (entry.getUnit().getId() == unitId){
                    return entry;
                }
            }
        }
        return null;
    }

    public Long getId() {
        return id;
    }

    public String getPatchVersion() {
        return patchVersion;
    }

    public Long getSeed() {
        return seed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Pool> getPools() {
        return pools;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public void setPools(List<Pool> pools) {
        this.pools = pools;
    }

    public Unit getUnitById(long unitId) {
        for (Pool pool: pools){
            for (PoolEntry entry: pool.getEntries()){
                if (entry.getUnit().getId() == unitId){
                    return entry.getUnit();
                }
            }
        }
        return null;
    }
}
