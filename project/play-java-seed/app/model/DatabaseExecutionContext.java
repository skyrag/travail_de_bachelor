package model;

import org.apache.pekko.actor.ActorSystem;
import play.api.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;

public class DatabaseExecutionContext extends CustomExecutionContext {
    @Inject
    public DatabaseExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "database.dispatcher");
    }
}
