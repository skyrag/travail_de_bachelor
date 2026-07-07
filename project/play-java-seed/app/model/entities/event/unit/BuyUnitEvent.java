package model.entities.event.unit;

import jakarta.persistence.Entity;
import model.entities.Round;
import model.entities.unit.InstanceUnit;

/**
 * An event that extends UnitEvent. It is used to describe the user event to buy a unit
 */
@Entity
public class BuyUnitEvent extends UnitEvent{

    protected BuyUnitEvent(){
        super();
    }

    public BuyUnitEvent(InstanceUnit unit, int step, Round round) {
        super(unit, step, round);
    }
}
