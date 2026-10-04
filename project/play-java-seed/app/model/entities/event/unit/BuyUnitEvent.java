package model.entities.event.unit;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import model.entities.Round;
import model.entities.unit.InstanceUnit;

/**
 * An event that extends UnitEvent. It is used to describe the user event to buy a unit
 */
@Entity
@Table(name = "buy_unit_event")
public class BuyUnitEvent extends UnitEvent{

    protected BuyUnitEvent(){
        super();
    }

    public BuyUnitEvent(InstanceUnit unit, int step, Round round) {
        super(unit, step, round);
    }
}
