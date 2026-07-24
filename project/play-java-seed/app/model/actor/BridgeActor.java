package model.actor;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import org.apache.pekko.actor.typed.ActorRef;

/**
 * Gateway actor between a standard WebSocket connection (Pekko API)
 * and a typed actor ({@link ConnexionActor}).
 * <p>
 * It receives JSON messages from the client, converts them into
 * {@link ConnexionActor.IncomingMessage}, and forwards them to the
 * connection actor. When the connection is closed, it also notifies the
 * {@code ConnexionActor}.
 */
public class BridgeActor extends AbstractActor {
    private final ActorRef<ConnexionActor.Message> userActor;
    private final org.apache.pekko.actor.ActorRef out;

    /**
     * Creates the Props needed to instantiate a {@code BridgeActor}.
     *
     * @param out actor representing the WebSocket output
     * @param actor typed actor responsible for handling connection messages
     * @return the props for creating a {@code BridgeActor}
     */
    public static Props create(org.apache.pekko.actor.ActorRef out, ActorRef<ConnexionActor.Message> actor) {
        return Props.create(BridgeActor.class, () -> new BridgeActor(out, actor));
    }

    /**
     * Constructs a new gateway actor.
     *
     * @param out actor representing the WebSocket output
     * @param userActor typed actor receiving messages from the client
     */
    public BridgeActor(org.apache.pekko.actor.ActorRef out, ActorRef<ConnexionActor.Message> userActor) {
        this.userActor = userActor;
        this.out = out;
    }

    /**
     * Defines the actor's behavior.
     * <p>
     * Each received JSON message is wrapped in a
     * {@link ConnexionActor.IncomingMessage} and forwarded to the
     * connection actor.
     *
     * @return the receive behavior for messages
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JsonNode.class, msg ->
                        userActor.tell(new ConnexionActor.IncomingMessage(msg))
                )

                .build();
    }

    /**
     * Called automatically when the actor stops.
     * <p>
     * Informs the connection actor that the WebSocket has been closed so it
     * can perform necessary cleanup.
     */
    @Override
    public void postStop() {
        userActor.tell(ConnexionActor.ConnectionClosed.INSTANCE);
    }
}
