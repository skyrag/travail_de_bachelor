package model.entities;

import jakarta.persistence.*;
import model.entities.game.Rarity;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "shop_level")
public class LevelData {

    @EmbeddedId
    private ShopLevelId id;

    @Column(name = "common_chances", nullable = false)
    private Integer commonChances;

    @Column(name = "uncommon_chances", nullable = false)
    private Integer uncommonChances;

    @Column(name = "rare_chance", nullable = false)
    private Integer rareChance;

    @Column(name = "epic_chance", nullable = false)
    private Integer epicChance;

    @Column(name = "legendary_chances", nullable = false)
    private Integer legendaryChances;

    @Column(name = "nextlvl", nullable = false)
    private Integer nextLvl;

    protected LevelData() {}

    public LevelData(ShopLevelId id, int commonChances, int uncommonChances,
                     int rareChance, int epicChance, int legendaryChances, int nextLvl) {
        this.id = id;
        this.commonChances = commonChances;
        this.uncommonChances = uncommonChances;
        this.rareChance = rareChance;
        this.epicChance = epicChance;
        this.legendaryChances = legendaryChances;
        this.nextLvl = nextLvl;
    }

    public int getChance(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> commonChances;
            case UNCOMMON -> uncommonChances;
            case RARE -> rareChance;
            case EPIC -> epicChance;
            case LEGENDARY -> legendaryChances;
        };
    }

    public Integer getNextLvl() { return nextLvl; }
    public ShopLevelId getId() { return id; }

    @Embeddable
    public static class ShopLevelId implements Serializable {

        @Column(name = "lvl", nullable = false)
        private Integer lvl;

        @Column(name = "patch_version", nullable = false, length = 20)
        private String patchVersion;

        protected ShopLevelId() {}

        public ShopLevelId(int lvl, String patchVersion) {
            this.lvl = lvl;
            this.patchVersion = patchVersion;
        }

        public Integer getLvl() { return lvl; }
        public String getPatchVersion() { return patchVersion; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ShopLevelId s)) return false;
            return Objects.equals(lvl, s.lvl) && Objects.equals(patchVersion, s.patchVersion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lvl, patchVersion);
        }
    }
}