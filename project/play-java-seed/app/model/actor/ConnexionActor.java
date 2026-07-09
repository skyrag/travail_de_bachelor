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

/**
 * The websocket communicate using json that has this form :
 * {
 *   "id" : "",
 *   "type": "",
 *   "paylod": {
 *     "entity1" : "",
 *     "entity2" : ""
 *   }
 * }
 */
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

    public static final class SendFight implements Message {
        public JsonNode fight;
        public SendFight(JsonNode fight){
            this.fight = fight;
        }
    }

    public static final class FeedbackInput implements Message{
        public JsonNode feedback;
        public FeedbackInput(JsonNode feedback) {
            this.feedback = feedback;
        }
    }

    public static final class EndGame implements Message{
        public String winner;
        public EndGame(String winner){
            this.winner = winner;
        }
    }

    private org.apache.pekko.actor.ActorRef ws;
    public long userId;

    public static Behavior<Message> create(org.apache.pekko.actor.ActorRef ws, long userId) {
        return Behaviors.setup(ctx -> new ConnexionActor(ctx, ws, userId));
    }

    private ConnexionActor(ActorContext<Message> ctx, org.apache.pekko.actor.ActorRef ws, long userId) {
        super(ctx);
        this.ws = ws;
        this.userId = userId;
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
        switch (msg.text.get("type")){
            default -> ws.tell(Json.newObject().put("type", "error").put("message" , "unknown type found"),
                    org.apache.pekko.actor.ActorRef.noSender());
        }
        return Behaviors.same();
    }

    private Behavior<Message> onConnectionClosed(ConnectionClosed msg) {
        // implémenter un timer pour que cet acteur se tue après un certains temps une fois que la connexion est terminer
        return Behaviors.same();
    }

}
