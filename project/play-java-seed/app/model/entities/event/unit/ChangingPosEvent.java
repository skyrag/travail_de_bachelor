package model.entities.event.unit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import model.utils.Tuple;

@Entity
public class ChangingPosEvent extends UnitEvent{

    @Column(nullable = false)
    private Tuple position;

    public Tuple getPosition() {
        return position;
    }

    public void setPosition(Tuple position) {
        this.position = position;
    }
}
