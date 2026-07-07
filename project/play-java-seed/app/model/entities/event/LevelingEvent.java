package model.entities.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import model.entities.Round;

/**
 * An event that extends Event.
 * <p>
 * It represents an event that changes the experience of a user
 */
@Entity
public class LevelingEvent extends Event{

    @Column(nullable = false)
    private Integer level;

    protected LevelingEvent () {
        super();
    }

    public LevelingEvent(int step, Round round, int exp){
        super(step, round);
        this.level = exp;
    }


    // getter/setter
    public int getLevel() {
        return level;
    }
}
