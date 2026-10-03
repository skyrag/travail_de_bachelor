package model.repositories;

import model.DTO.ItemDTO;
import model.DTO.ItemDTOMapper;
import model.DatabaseExecutionContext;
import model.entities.Round;
import model.entities.Team;
import model.entities.game.Game;
import model.entities.unit.AbilityFragment;
import model.entities.unit.Item;
import model.entities.unit.Unit;
import org.hibernate.Hibernate;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
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
}
