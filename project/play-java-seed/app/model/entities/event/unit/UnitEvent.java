package model.entities.event.unit;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import model.entities.Round;
import model.entities.event.Event;
import model.entities.unit.InstanceUnit;
import model.entities.unit.Unit;

/**
 * Base event for all unit related event in the game.
 * <p>
 * Uses InheritanceType#JOINED so each subclass is mapped to its
 * own table, joined on the primary key with the base table of event.
 */
@Entity
public abstract class UnitEvent extends Event {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private InstanceUnit unit;

    protected UnitEvent() {
        super();
    }

    protected UnitEvent (InstanceUnit unit, int step, Round round) {
        super(step, round);
        this.unit = unit;
    }


    // getter/setter
    public InstanceUnit getUnit() {
        return unit;
    }
}
