package model.entities.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import model.entities.Round;

/**
 * An event that extends Event.
 * <p>
 * It represents an event that changes the gold of a user
 */
@Entity
public class ChangingGoldEvent extends Event{

    @Column(nullable = false)
    private Integer gold;

    protected ChangingGoldEvent() {
        super();
    }

    public ChangingGoldEvent(int step, Round round, int gold) {
        super(step, round);
        this.gold = gold;
    }


    //getter/setter
    public int getGold() {
        return gold;
    }
}
