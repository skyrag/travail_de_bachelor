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
    private PoolRarity poolsRarity;

    @OneToMany(mappedBy = "pool", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PoolEntry> entries = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public PoolRarity getPoolsRarity() {
        return poolsRarity;
    }

    public void setPoolsRarity(PoolRarity poolsRarity) {
        this.poolsRarity = poolsRarity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<PoolEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<PoolEntry> entries) {
        this.entries = entries;
    }
}
