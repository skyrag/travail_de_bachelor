package model.repositories;

import model.DatabaseExecutionContext;
import model.entities.User;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * JPA implementation of UserRepo.
 * <p>
 * This repository uses Play Framework's JPAApi to perform
 * database operations on User entities. All public methods
 * are executed asynchronously using the provided
 * DatabaseExecutionContext.
 * <p>
 * Each operation is executed within its own transaction through
 * JPAApi#withTransaction(Function)
 */
public class LoginRepository extends BasicRepository{

    @Inject
    public LoginRepository(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
    }

    /**
     * Get all the Users from the DB
     *
     * @return a CompletionStage containing the list of users
     */
    public CompletionStage<List<User>> getAllUsers() {
        return supplyAsync(() -> wrap(em ->
                em.createQuery("select u from User u", User.class).getResultList()
        ), executionContext);
    }

    /**
     * Get a user from the DB based on his email
     *
     * @param email the email to check
     * @return a CompletionStage containing the user
     */
    public CompletionStage<User> getByEmail(String email) {
        return supplyAsync(() -> wrap(em -> {
            List<User> list = em.createQuery(
                            "select u from User u where u.email = :email", User.class).setParameter("email", email)
                    .setMaxResults(1)
                    .getResultList();
            return list.isEmpty()? null : list.getFirst();
        }
        ), executionContext);
    }

    /**
     * Get a user from the DB based on his username
     *
     * @param username the username to check
     * @return a CompletionStage containing the user
     */
    public CompletionStage<User> getByUsername(String username) {
        return supplyAsync(() -> wrap(em -> {
            List<User> list = em.createQuery(
                            "select u from User u where u.username = :username", User.class).setParameter("username", username)
                    .setMaxResults(1)
                    .getResultList();
            return list.isEmpty()? null : list.getFirst();
        }
        ), executionContext);
    }
}
