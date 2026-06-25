package model;

import org.apache.pekko.actor.ActorSystem;
import play.api.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;
/**
 * Execution context dedicated to database operations.
 * <p>
 * This class provides a custom Akka dispatcher used to execute
 * blocking JPA/database operations off the main application thread,
 * preventing performance issues and thread starvation.
 * <p>
 * It is configured via the "database.dispatcher" configuration
 * in the application configuration file in the conf directory.
 */
public class DatabaseExecutionContext extends CustomExecutionContext {

    /**
     * Creates a new database execution context using the configured
     * "database.dispatcher" Akka dispatcher.
     *
     * @param actorSystem the Play/Akka ActorSystem used to resolve dispatchers
     */
    @Inject
    public DatabaseExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "database.dispatcher");
    }
}
