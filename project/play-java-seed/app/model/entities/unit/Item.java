package model.entities.unit;

import jakarta.persistence.*;
import model.entities.effect.StatChangingEffect;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an in-game item/object that
 * a unit can hold or equip, tied to a specific patchVersion of
 * the game's balancing data.
 * <p>
 * An Object has a name, a description, and a list of
 * StatChangingEffect it grants when equipped/used.
 */
@Entity
@Table(name = "object")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patch_version", nullable = false, length = 20)
    private String patchVersion;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToMany
    @JoinTable(
            name = "object_effect",
            joinColumns = @JoinColumn(name = "object_id"),
            inverseJoinColumns = @JoinColumn(name = "effect_id")
    )
    private List<StatChangingEffect> effects = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    protected Item(){

    }

    public Item(String patchVersion, String name, String description, List<StatChangingEffect> effects){
        this.patchVersion = patchVersion;
        this.name = name;
        this.description = description;
        this.effects = effects;
    }


    //getter/setter
    public Long getId() {
        return id;
    }

    public String getPatchVersion() {
        return patchVersion;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<StatChangingEffect> getEffects() {
        return effects;
    }

}
