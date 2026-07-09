package model.entities.game;

import jakarta.persistence.*;
import model.entities.unit.Unit;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents, for a given Pool, how many copies of a specific
 * Unit remain available to be drawn.
 * <p>
 * Uses a composite key (pool_id, unit_id) via
 * PoolEntryId, since there is exactly one entry per unit per
 * pool.
 */
@Entity
@Table(name = "pool_entry")
@IdClass(PoolEntry.PoolEntryId.class)
public class PoolEntry {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(nullable = false)
    private Integer number;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    protected PoolEntry() {

    }

    public PoolEntry(Pool pool, Unit unit, int number) {
        this.pool = pool;
        this.unit = unit;
        this.number = number;
    }

    public void increment(){
        number++;
    }

    public void decrement(){
        number--;
    }

    //getter/setter
    public Unit getUnit() {
        return unit;
    }

    public Integer getNumber() {
        return number;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Composite primary key for PoolEntry, combining the pool's
     * id and the unit's id.
     */
    public static class PoolEntryId implements Serializable {
        private Long pool;
        private Long unit;

        public PoolEntryId() {
        }

        public PoolEntryId(Long pool, Long unit) {
            this.pool = pool;
            this.unit = unit;
        }

        public Long getPool() {
            return pool;
        }

        public void setPool(Long pool) {
            this.pool = pool;
        }

        public Long getUnit() {
            return unit;
        }

        public void setUnit(Long unit) {
            this.unit = unit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PoolEntryId that)) return false;
            return java.util.Objects.equals(pool, that.pool) && java.util.Objects.equals(unit, that.unit);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(pool, unit);
        }
    }
}
