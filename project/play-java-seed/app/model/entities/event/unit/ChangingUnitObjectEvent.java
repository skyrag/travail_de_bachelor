package model.entities.event.unit;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import model.entities.unit.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * An event that extends UnitEvent. It contains objects, and this event describes
 * the new set of objects that a user want a unit to have
 */
@Entity
public class ChangingUnitObjectEvent extends UnitEvent{

    @ManyToMany
    @JoinTable(
            name = "changing_unit_object_events_object",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "objects_id")
    )
    private List<Item> items = new ArrayList<>();

    public List<Item> getObjects() {
        return items;
    }

    public void setObjects(List<Item> items) {
        this.items = items;
    }

}
