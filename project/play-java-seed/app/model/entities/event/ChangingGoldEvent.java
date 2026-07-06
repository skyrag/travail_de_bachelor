package model.entities.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

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
