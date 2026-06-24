package model.repositories;

import jakarta.persistence.Entity;
import model.DatabaseExecutionContext;
import model.entities.User;
import play.db.jpa.JPAApi;

import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.supplyAsync;

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
                em.find(User.class, user.id)
        ), executionContext);
    }

    @Override
    public CompletionStage<Boolean> exists(String email, String username) {
        if (email == null) {
            return supplyAsync(() -> wrap(em -> !em.createQuery(
                    "select u from User u where u.username == username", User.class).getResultList().isEmpty()), executionContext);
        } else {
            return supplyAsync(() -> wrap(em -> !em.createQuery(
                    "select u from User u where u.email == email", User.class).getResultList().isEmpty()), executionContext);
        }
    }

    private <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }

    private User insert(EntityManager em, User user) {
        em.persist(user);
        return user;
    }
}
