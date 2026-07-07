package model.entities.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import model.entities.Round;

/**
 * An event that extends Event.
 * <p>
 * It represents an event that changes the health of a user
 */
@Entity
public class LosingHealthEvent extends Event{

    @Column(nullable = false)
    private Integer health;

    protected LosingHealthEvent() {
        super();
    }

    public LosingHealthEvent(int step, Round round, int health) {
        super(step, round);
        this.health = health;
    }


    //getter/setter
    public int getHealth() {
        return health;
    }
}
