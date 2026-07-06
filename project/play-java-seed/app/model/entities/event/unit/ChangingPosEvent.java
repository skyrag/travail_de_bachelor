package model.entities.event.unit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import model.utils.Tuple;
import org.hibernate.annotations.Type;

/**
 * An event that extends UnitEvent. It has position that is a tuple
 * used to described the changes that a user want a unit to have
 */
@Entity
public class ChangingPosEvent extends UnitEvent{

    @Type(Tuple.TupleType.class)
    @Column(nullable = false)
    private Tuple position;

    public Tuple getPosition() {
        return position;
    }

    public void setPosition(Tuple position) {
        this.position = position;
    }
}
