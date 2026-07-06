package model.entities.event.unit;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import model.entities.unit.Object;
import model.entities.unit.Unit;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ChangingUnitObjectEvent extends UnitEvent{

    @ManyToMany
    @JoinTable(
            name = "changing_unit_object_events_object",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "objects_id")
    )
    private List<Object> objects = new ArrayList<>();

    public List<Object> getObjects() {
        return objects;
    }

    public void setObjects(List<Object> objects) {
        this.objects = objects;
    }

}
