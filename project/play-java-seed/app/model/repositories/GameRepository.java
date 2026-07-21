package model.repositories;

import jakarta.persistence.EntityManager;
import model.DatabaseExecutionContext;
import model.entities.User;
import model.entities.game.Game;
import model.entities.unit.Item;
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
    public CompletionStage<List<Item>> getAllItems() {
        return supplyAsync(() -> wrap(em ->
                em.createQuery("select u from object u", Item.class).getResultList()
        ), executionContext);
    }
}
