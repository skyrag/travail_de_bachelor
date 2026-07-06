package model.entities.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class LosingHealthEvent extends Event{

    @Column(nullable = false)
    private int health;

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}
