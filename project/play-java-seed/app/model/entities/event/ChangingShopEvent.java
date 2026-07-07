package model.entities.event;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import model.entities.Round;
import model.entities.unit.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * An event that extends Event
 * <p>
 * it represents an event where we change the shop of a user with the new ones contained in units
 */
@Entity
public class ChangingShopEvent extends Event{

    @ManyToMany
    @JoinTable(
            name = "changing_shops_unit",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "unit_id")
    )
    private List<Unit> units = new ArrayList<>();

    protected ChangingShopEvent() {
        super();
    }

    public ChangingShopEvent(int step, Round round, List<Unit> units) {
        super(step, round);
        this.units = units;
    }


    //getter/setter
    public List<Unit> getUnits() {
        return units;
    }
}

