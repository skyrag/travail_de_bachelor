package model.entities.event.unit;

import jakarta.persistence.Entity;

/**
 * An event that extends UnitEvent. It is used to describe the user event to sell a unit
 */
@Entity
public class SellUnitEvent extends UnitEvent{
}
