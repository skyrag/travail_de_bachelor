package model.entities.event.unit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import model.entities.Round;
import model.entities.unit.InstanceUnit;
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

    protected ChangingPosEvent() {
        super();
    }

    public ChangingPosEvent (InstanceUnit unit, int step, Round round, Tuple position) {
        super(unit, step, round);
        this.position = position;
    }



    // getter/setter
    public Tuple getPosition() {
        return position;
    }
}
