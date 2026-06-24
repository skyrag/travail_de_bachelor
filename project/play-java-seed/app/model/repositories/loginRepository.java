package model.repositories;

import model.DatabaseExecutionContext;
import model.entities.User;
import play.db.jpa.JPAApi;

import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * JPA implementation of {@link UserRepo}.
 * <p>
 * This repository uses Play Framework's {@link JPAApi} to perform
 * database operations on {@link User} entities. All public methods
 * are executed asynchronously using the provided
 * {@link DatabaseExecutionContext}.
 * <p>
 * Each operation is executed within its own transaction through
 * {@link JPAApi#withTransaction(Function)}
 */
public class loginRepository implements UserRepo{

    private final JPAApi jpaApi;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public loginRepository(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<User> add(User user) {
        return supplyAsync(() -> wrap(em -> insert(em, user)), executionContext);
    }

    @Override
    public CompletionStage<List<User>> getAll() {
        return supplyAsync(() -> wrap(em ->
                em.createQuery("select u from User u", User.class).getResultList()
        ), executionContext);
    }

    @Override
    public CompletionStage<User> get(User user) {
        return supplyAsync(() -> wrap(em ->
                em.find(User.class, user.getId())
        ), executionContext);
    }

    @Override
    public CompletionStage<Boolean> existsByEmail(String email) {
        return supplyAsync(() -> wrap(em -> !em.createQuery(
                "select u from User u where u.email = :email", User.class).setParameter("email", email)
                .getResultList()
                .isEmpty()
        ), executionContext);
    }

    @Override
    public CompletionStage<Boolean> existsByUsername(String username) {
        return supplyAsync(() -> wrap(em -> !em.createQuery(
                "select u from User u where u.username = :username", User.class).setParameter("username", username)
                .getResultList()
                .isEmpty()
        ), executionContext);
    }

    @Override
    public CompletionStage<User> remove(User user){
        return supplyAsync(() -> wrap(em ->  remove(em, user)));
    }

    /**
     * Executes the provided function inside a JPA transaction.
     *
     * @param function the operation to execute
     * @param <T> the type returned by the operation
     * @return the result of the operation
     */
    private <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }

    /**
     * Persists a user entity.
     *
     * @param em the active entity manager
     * @param user the user to persist
     * @return the persisted user
     */
    private User insert(EntityManager em, User user) {
        em.persist(user);
        return user;
    }

    /**
     * Removes a user entity from the database.
     * <p>
     * The user is first merged into the current persistence context
     * to ensure that detached entities can be removed safely.
     *
     * @param em the active entity manager
     * @param user the user to remove
     * @return the removed user
     */
    private User remove(EntityManager em, User user) {
        em.remove(em.merge(user));
        return user;
    }
}
