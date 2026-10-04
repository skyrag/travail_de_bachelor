package model.repositories;

import model.DTO.ItemDTO;
import model.DTO.ItemDTOMapper;
import model.DatabaseExecutionContext;
import model.entities.Round;
import model.entities.Team;
import model.entities.event.Event;
import model.entities.game.Game;
import model.entities.unit.AbilityFragment;
import model.entities.unit.InstanceUnit;
import model.entities.unit.Item;
import model.entities.unit.Unit;
import model.utils.Tuple;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class GameRepository extends BasicRepository{

    @Inject
    public GameRepository(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
    }


    /**
     * Get all the Items from the DB
     *
     * @return a CompletionStage containing the list of items
     */
    public CompletionStage<List<ItemDTO>> getAllItemsDTO() {
        return supplyAsync(() -> wrap(em -> {
            List<Item> items = em.createQuery("select distinct i from Item i left join fetch i.effects", Item.class).getResultList();

            return items.stream().map(ItemDTOMapper::itemToDTO).toList();
        }
        ), executionContext);
    }

    public CompletionStage<Game> persistGameSetup(Game game) {
        return supplyAsync(() -> wrap(em -> {
            Game merged = em.merge(game);   // insère les InstanceUnit via le cascade
            em.flush();                     // les ids sont générés ici

            em.createQuery(
                            "select distinct u from Unit u " +
                                    "left join fetch u.ability a " +
                                    "left join fetch a.strategie", Unit.class)
                    .getResultList();
            em.createQuery(
                            "select distinct a from AbilityFragment a left join fetch a.effects", AbilityFragment.class)
                    .getResultList();
            em.createQuery(
                            "select distinct i from Item i left join fetch i.effects", Item.class)
                    .getResultList();

            em.createQuery(
                            "select distinct t from Team t " +
                                    "join fetch t.user " +
                                    "where t.game = :g", Team.class)
                    .setParameter("g", merged)
                    .getResultList();

            return merged;
        }), executionContext);
    }

    public CompletionStage<Void> persistNewRounds(List<Round> rounds) {
        return supplyAsync(() -> wrap(em -> {
            for (Round r : rounds) {
                em.persist(r);
            }
            return null;
        }), executionContext);
    }

    public CompletionStage<Void> persistBuy(Team.Result r, Team team, List<Unit> shop, int gold) {
        return supplyAsync(() -> wrap(em -> {
            em.persist(r.instance());
            em.persist(r.event());

            Team managed = em.find(Team.class, team.getId());
            managed.setGold(gold);
            managed.setShop(new ArrayList<>(shop));
            return null;
        }), executionContext);
    }

    public CompletionStage<Void> persistSell(Team.Result r, Team team, int gold, List<Item> items){
        return supplyAsync(() -> wrap(em -> {
            System.out.println("on persist");
            InstanceUnit managedUnit = em.find(InstanceUnit.class, r.instance().getId());
            managedUnit.markSold();
            managedUnit.getItems().clear();

            em.persist(r.event());

            Team managed = em.find(Team.class, team.getId());
            managed.setGold(gold);
            managed.setItems(new ArrayList<>(items));
            return null;
        }), executionContext);
    }

    public CompletionStage<Void> persistMove(Team.Result r, Tuple tuple) {
        return supplyAsync(() -> wrap(em -> {
            InstanceUnit managed = em.find(InstanceUnit.class, r.instance().getId());
            managed.setPos(tuple);
            em.persist(r.event());
            return null;
        }), executionContext);
    }

    public CompletionStage<Void> persistGive(Team.Result r, Team team, List<Item> insatnceItems, List<Item> teamItems){
        return supplyAsync(() -> wrap(em -> {
            InstanceUnit managedU = em.find(InstanceUnit.class, r.instance().getId());
            managedU.setItems(new ArrayList<>(insatnceItems));
            em.persist(r.event());

            Team managed = em.find(Team.class, team.getId());
            managed.setItems(new ArrayList<>(teamItems));
            return null;
        }), executionContext);
    }

    public CompletionStage<Void> persistExp(Team team, Event e, int gold, int lvl, int exp){
        return supplyAsync(() -> wrap(em -> {
            em.persist(e);

            Team managed = em.find(Team.class, team.getId());
            managed.setGold(gold);
            managed.setLvl(lvl);
            managed.setExp(exp);
            return null;
        }), executionContext);
    }

    public CompletionStage<Void> persistReroll (Team team, Event e, int gold, List<Unit> shop) {
        return supplyAsync(() -> wrap(em -> {
            em.persist(e);

            Team managed = em.find(Team.class, team.getId());
            managed.setGold(gold);
            managed.setShop(new ArrayList<>(shop));
            return null;
        }), executionContext);
    }
}
