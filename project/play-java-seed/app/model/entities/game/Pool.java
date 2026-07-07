package model.entities.game;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the pool of units of a given PoolRarity available
 * to draw from within a Game.
 * <p>
 * A Pool tracks the available PoolEntry items for its
 * rarity tier (e.g. how many copies of each unit remain to be drawn),
 * and is used when rolling/refreshing shops for teams.
 */
@Entity
@Table(name = "pool")
public class Pool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(name = "pools_rarity", nullable = false)
    private Rarity poolsRarity;

    @OneToMany(mappedBy = "pool", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PoolEntry> entries = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    protected Pool(){

    }

    public Pool(Game game, Rarity rarity, List<PoolEntry> entries){
        this.game = game;
        this.poolsRarity = rarity;
        this.entries = entries;
    }




    //getter/setter
    public Long getId() {
        return id;
    }

    public Rarity getPoolsRarity() {
        return poolsRarity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<PoolEntry> getEntries() {
        return entries;
    }
}
