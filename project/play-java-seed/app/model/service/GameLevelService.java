package model.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import model.entities.LevelData;
import model.entities.game.Rarity;
import play.db.jpa.JPAApi;

import java.util.List;

@Singleton
public class GameLevelService {

    private List<LevelData> levelsData;

    @Inject
    public GameLevelService(JPAApi jpaApi, com.typesafe.config.Config config) {
        // chargement au démarrage
        this.levelsData = jpaApi.withTransaction(em -> {
            return em.createQuery(
                            "SELECT s FROM LevelData s", LevelData.class)
                    .getResultList();
        });
    }

    public int getProba(int level, Rarity rarity) {

        System.out.println(levelsData.get(level).getId());
        return levelsData.get(level).getChance(rarity);
    }

    public int getExpRequired(int level) {

        System.out.println(levelsData.get(level).getId());
        return levelsData.get(level).getNextLvl();
    }
}
