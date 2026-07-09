package model.repositories;

import jakarta.persistence.EntityManager;
import model.DatabaseExecutionContext;
import model.entities.User;
import model.entities.game.Game;
import play.db.jpa.JPAApi;

import javax.inject.Inject;

public class GameRepository extends BasicRepository{

    @Inject
    public GameRepository(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
    }

}
