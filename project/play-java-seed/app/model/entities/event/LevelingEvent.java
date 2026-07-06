package model.entities.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class LevelingEvent extends Event{

    @Column(nullable = false)
    private int level;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
