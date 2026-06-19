package model.repositories;

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
    public JPAPersonRepository(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<User> add(User user) {
        return supplyAsync(() -> wrap(em -> insert(em, user)), executionContext);
    }

    @Override
    public CompletionStage<List<User>> getAll() {
        return null;
    }

    @Override
    public CompletionStage<User> get(User user) {
        return null;
    }

    @Override
    public CompletionStage<User> exists(String email, String username) {
        return null;
    }

    private <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }

    private User insert(EntityManager em, User user) {
        em.persist(user);
        return user;
    }
}
