package model.entities.event.unit;

import jakarta.persistence.Entity;
import model.entities.Round;
import model.entities.unit.InstanceUnit;

/**
 * An event that extends UnitEvent. It is used to describe the user event to sell a unit
 */
@Entity
public class SellUnitEvent extends UnitEvent{

    protected SellUnitEvent() {
        super();
    }

    public SellUnitEvent(InstanceUnit unit, int step, Round round) {
        super(unit, step, round);
    }
}
