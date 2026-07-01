package model.actor;

import com.fasterxml.jackson.databind.JsonNode;
import model.entities.User;
import org.apache.pekko.actor.Actor;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.SupervisorStrategy;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import play.libs.Json;
import scala.Option;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.time.Duration;

public class ConnexionActor extends AbstractBehavior<ConnexionActor.Message> {

    public interface Message{}

    public static final class IncomingMessage implements Message {
        public final JsonNode text;
        public IncomingMessage(JsonNode text) { this.text = text; }
    }

    public static final class ConnectionClosed implements Message {
        public static final ConnectionClosed INSTANCE = new ConnectionClosed();
        private ConnectionClosed() {}
    }

    private org.apache.pekko.actor.ActorRef ws;

    public static Behavior<Message> create(org.apache.pekko.actor.ActorRef ws) {
        return Behaviors.setup(ctx -> new ConnexionActor(ctx, ws));
    }

    private ConnexionActor(ActorContext<Message> ctx, org.apache.pekko.actor.ActorRef ws) {
        super(ctx);
        this.ws = ws;
        System.out.println("banger2");
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(IncomingMessage.class, this::onIncoming)
                .onMessage(ConnectionClosed.class, this::onConnectionClosed)
                .build();
    }

    private Behavior<Message> onIncoming(IncomingMessage msg) {
        // faire un traitement des message une fois qu'on a la game
        return Behaviors.same();
    }

    private Behavior<Message> onConnectionClosed(ConnectionClosed msg) {
        // implémenter un timer pour que cet acteur se tue après un certains temps une fois que la connexion est terminer
        return Behaviors.same();
    }

}
