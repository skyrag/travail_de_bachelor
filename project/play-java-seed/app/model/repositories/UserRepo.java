package model.repositories;

import com.google.inject.ImplementedBy;
import model.entities.User;

import java.util.List;
import java.util.concurrent.CompletionStage;

@ImplementedBy(loginRepository.class)
public interface UserRepo {

    CompletionStage<User> add(User user);
    
    CompletionStage<List<User>> getAll();
    
    CompletionStage<User> get(User user);

    CompletionStage<Boolean> exists(String email, String username);
}
