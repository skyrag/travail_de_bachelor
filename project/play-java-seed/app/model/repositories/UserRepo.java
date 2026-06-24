package model.repositories;

import com.google.inject.ImplementedBy;
import model.entities.User;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Repository responsible for managing User entities.
 * <p>
 * Provides asynchronous CRUD operations and utility methods
 * for checking the existence of users based on their email
 * address or username.
 */
@ImplementedBy(LoginRepository.class)
public interface UserRepo {

    /**
     * Persists a new user.
     *
     * @param user the user to add
     * @return a CompletionStage containing the persisted user
     */
    CompletionStage<User> add(User user);

    /**
     * Removes a user.
     *
     * @param user the user to remove
     * @return a CompletionStage containing the removed user
     */
    CompletionStage<User> remove(User user);

    /**
     * Get all the Users from the DB
     *
     * @return a CompletionStage containing the list of users
     */
    CompletionStage<List<User>> getAll();

    /**
     * Get a specific user from the DB
     *
     * @param user the user to get
     * @return a CompletionStage containing the user
     */
    CompletionStage<User> get(User user);


    /**
     * Checks whether a user already has this email
     *
     * @param email the email to check
     * @return a CompletionStage containing the boolean
     */
    CompletionStage<User> getByEmail(String email);

    /**
     * Checks whether a user already has this username
     *
     * @param username the username to check
     * @return a CompletionStage containing the boolean
     */
    CompletionStage<User> getByUsername(String username);
}
