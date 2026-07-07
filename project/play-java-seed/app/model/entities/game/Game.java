package model.entities.game;

import jakarta.persistence.*;
import model.entities.Team;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public Game(String patchVersion, Long seed, List<Pool> pools, List<Team> teams){
        this.patchVersion = patchVersion;
        this.seed = seed;
        this.pools = pools;
        this.teams = teams;
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
}
