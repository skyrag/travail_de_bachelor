package model.repositories;

import model.DTO.ItemDTO;
import model.DTO.ItemDTOMapper;
import model.DatabaseExecutionContext;
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
    public CompletionStage<List<ItemDTO>> getAllItemsDTO() {
        return supplyAsync(() -> wrap(em -> {
            List<Item> items = em.createQuery("select distinct i from Item i left join fetch i.effects", Item.class).getResultList();

            return items.stream().map(ItemDTOMapper::itemToDTO).toList();
        }
        ), executionContext);
    }
}
