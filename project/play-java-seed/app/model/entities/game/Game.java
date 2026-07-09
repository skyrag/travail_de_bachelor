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

    protected Game() {

    }

    public Game(String patchVersion, Long seed){
        this.patchVersion = patchVersion;
        this.seed = seed;
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

    public List<Team> stillAlive(){
        List<Team> res= new ArrayList<>();
        for (Team team: teams){
            if (team.getHealth() > 0) {
                res.add(team);
            }
        }
        return res;
    }

    public void adjustRankings(){
        //TODO a revoir parce que la si un mec meurt après toi mais perd plus de pv alors il a une pire place
        teams.sort(Comparator.comparingInt(Team::getHealth));
        for (int i = 0; i < teams.size(); i++){
            teams.get(i).setRank(i);
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

    //getter/setter
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
}
