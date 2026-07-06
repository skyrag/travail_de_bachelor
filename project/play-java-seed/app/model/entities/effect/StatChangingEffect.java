package model.entities.effect;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class StatChangingEffect extends Effect{

    @Column(name = "type_change", nullable = false)
    private StatType typeChange;

    @Column(nullable = false)
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public StatType getTypeChange() {
        return typeChange;
    }

    public void setTypeChange(StatType typeChange) {
        this.typeChange = typeChange;
    }


}
