package model.repositories;

import jakarta.persistence.EntityManager;
import model.DatabaseExecutionContext;
import model.entities.User;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class BasicRepository {

    protected final JPAApi jpaApi;
    protected final DatabaseExecutionContext executionContext;

    @Inject
    public BasicRepository(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    /**
     * Persists a new object.
     *
     * @param persistable the object to add
     * @return a CompletionStage containing the persisted object
     */
    public <T> CompletionStage<T> add(T persistable) {
        return supplyAsync(() -> wrap(em -> insert(em, persistable)), executionContext);
    }


    /**
     * Removes an object.
     *
     * @param persistable the object to remove
     * @return a CompletionStage containing the removed object
     */
    public <T> CompletionStage<T> remove(T persistable) {
        return supplyAsync(() -> wrap(em -> remove(em, persistable)), executionContext);
    }

    public <T> CompletionStage<T> merge(T persistable) {
        return supplyAsync(()-> wrap(em -> merge(em, persistable)), executionContext);
    }

    public <T> CompletionStage<T> findById(Long id, Class<T> entityClass) {
        return supplyAsync(() -> wrap(em -> em.find(entityClass, id)), executionContext);
    }


    /**
     * Executes the provided function inside a JPA transaction.
     *
     * @param function the operation to execute
     * @param <T>      the type returned by the operation
     * @return the result of the operation
     */
    protected  <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }

    /**
     * Persists an object entity.
     * Be careful that what you want to persist is an entity
     *
     * @param em   the active entity manager
     * @param persistable the object to persist
     * @return the persisted object
     */
    protected <T> T insert(EntityManager em, T persistable) {
        em.persist(persistable);
        return persistable;
    }

    /**
     * Removes a persistable entity from the database.
     * <p>
     * The object is first merged into the current persistence context
     * to ensure that detached entities can be removed safely.
     * Be careful that what you want to persist is an entity
     *
     * @param em   the active entity manager
     * @param persistable the object to remove
     * @return the removed object
     */
    protected <T> T remove(EntityManager em, T persistable) {
        em.remove(em.merge(persistable));
        return persistable;
    }

    protected <T> T merge(EntityManager em, T persistable) {
        em.merge(persistable);
        return persistable;
    }
}
