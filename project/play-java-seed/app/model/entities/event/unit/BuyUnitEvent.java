package model.entities.event.unit;

import jakarta.persistence.Entity;

/**
 * An event that extends UnitEvent. It is used to describe the user event to buy a unit
 */
@Entity
public class BuyUnitEvent extends UnitEvent{
}
