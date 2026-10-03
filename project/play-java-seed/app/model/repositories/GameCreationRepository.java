package model.repositories;

import model.DatabaseExecutionContext;
import model.entities.Team;
import model.entities.User;
import model.entities.game.Game;
import model.entities.game.Pool;
import model.entities.game.PoolEntry;
import model.entities.game.Rarity;
import model.entities.unit.AbilityFragment;
import model.entities.unit.Item;
import model.entities.unit.Unit;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static model.utils.Constante.STARTINGGOLD;
import static model.utils.Constante.UNITSTARTINGPOOL;

public class GameCreationRepository extends BasicRepository{

    @Inject
    public GameCreationRepository(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
    }

    public CompletionStage<Game> createGame(String version, Long seed, List<Long> usersId) {
        return supplyAsync(() -> wrap(em -> {
            Game game = new Game(version, seed);

            List<Unit> units = em.createQuery(
                            "select distinct u from Unit u " +
                                    "left join fetch u.ability a " +
                                    "left join fetch a.strategie", Unit.class)
                    .getResultList();

            //remplit a.effects pour les fragments déjà chargés
            em.createQuery(
                            "select distinct a from AbilityFragment a " +
                                    "left join fetch a.effects " +
                                    "where a.unit in :units", AbilityFragment.class)
                    .setParameter("units", units)
                    .getResultList();

            List<Item> items = em.createQuery("select distinct i from Item i left join fetch i.effects", Item.class).getResultList();


            List<Team> teams = new ArrayList<>();
            for (long userId: usersId){
                List<User> user = em.createQuery("SELECT u FROM User u WHERE u.id = :userId", User.class)
                        .setParameter("userId", userId)
                        .setMaxResults(1)
                        .getResultList();

                if (user.isEmpty()){
                    throw new IllegalArgumentException("User not found: " + userId);
                }
                teams.add(new Team(user.getFirst(), game, STARTINGGOLD));
            }

            List<Pool> pools = new ArrayList<>();
            for(Rarity rarity: Rarity.values()) {
                pools.add(new Pool(game, rarity));
            }

            for (Unit unit: units){
                switch (unit.getRarity()){
                    case COMMON -> pools.get(0).addEntries(new PoolEntry(pools.get(0), unit, UNITSTARTINGPOOL));
                    case UNCOMMON -> pools.get(1).addEntries(new PoolEntry(pools.get(1), unit, UNITSTARTINGPOOL));
                    case RARE -> pools.get(2).addEntries(new PoolEntry(pools.get(2), unit, UNITSTARTINGPOOL));
                    case EPIC -> pools.get(3).addEntries(new PoolEntry(pools.get(3), unit, UNITSTARTINGPOOL));
                    case LEGENDARY -> pools.get(4).addEntries(new PoolEntry(pools.get(4), unit, UNITSTARTINGPOOL));
                }
            }

            game.setItems(items);
            game.setTeams(teams);
            game.setPools(pools);
            em.persist(game);
            return game;
        }));
    }
}
