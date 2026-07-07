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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatchVersion() {
        return patchVersion;
    }

    public void setPatchVersion(String patchVersion) {
        this.patchVersion = patchVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setEffects(List<StatChangingEffect> effects) {
        this.effects = effects;
    }

    public List<StatChangingEffect> getEffects() {
        return effects;
    }

}
