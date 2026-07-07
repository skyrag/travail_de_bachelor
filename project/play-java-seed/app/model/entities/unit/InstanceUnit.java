package model.entities.unit;

import jakarta.persistence.*;
import model.entities.Team;
import model.utils.Tuple;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * An instance unit is litterally an instance of a unit used to play the game.
 * It helps keeping track of any changes that may have happened during the game
 * on the unit used by the user.
 * It has the unit it is based of, the object wore by the unit, the level of the unit
 * the position of the unit, and the team it belongs to.
 * <p>
 * The main point of this class is to have a base state between round so that
 * it is easier to reload the current state in case of crash with the help of event too
 */
@Entity
@Table(name = "instance_unit")
public class InstanceUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer lvl;

    @Type(Tuple.TupleType.class)
    @Column(nullable = false)
    private Tuple pos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToMany
    @JoinTable(
            name = "instance_units_object",
            joinColumns = @JoinColumn(name = "instance_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "object_id")
    )
    private List<Item> items = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLvl() {
        return lvl;
    }

    public void setLvl(Integer lvl) {
        this.lvl = lvl;
    }

    public Tuple getPos() {
        return pos;
    }

    public void setPos(Tuple pos) {
        this.pos = pos;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Item> getObjects() {
        return items;
    }

    public void setObjects(List<Item> items) {
        this.items = items;
    }
}
