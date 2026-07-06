package model.entities.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * An event that extends Event.
 * <p>
 * It represents an event that changes the gold of a user
 */
@Entity
public class ChangingGoldEvent extends Event{

    @Column(nullable = false)
    private int gold;

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }
}
