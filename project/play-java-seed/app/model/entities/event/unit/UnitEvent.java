package model.entities.event.unit;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import model.entities.event.Event;
import model.entities.unit.InstanceUnit;
import model.entities.unit.Unit;

@Entity
public abstract class UnitEvent extends Event {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private InstanceUnit unit;

    public InstanceUnit getUnit() {
        return unit;
    }

    public void setUnit(InstanceUnit unit) {
        this.unit = unit;
    }
}
