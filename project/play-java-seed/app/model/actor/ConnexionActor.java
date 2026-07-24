package model.actor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.monitor.ActeurMonitor;
import model.service.MatchmakingService;
import model.utils.Tuple;
import org.apache.pekko.actor.Cancellable;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import play.libs.Json;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static model.actor.JsonConstantes.*;

/**
 * Actor representing a player's connection to the server.
 * <p>
 * This actor is responsible for:
 * <ul>
 *     <li>receiving messages from the WebSocket;</li>
 *     <li>forwarding the player's actions to the {@link GameActor};</li>
 *     <li>sending server messages to the client;</li>
 *     <li>handling reconnection and message buffering;</li>
 *     <li>monitoring connection health using a heartbeat mechanism.</li>
 * </ul>
 */
public class ConnexionActor extends AbstractBehavior<ConnexionActor.Message> {


    /**
     * Interface commune à tous les messages traités par le {@code ConnexionActor}.
     */
    public interface Message{}

    /**
     * Message contenant une requête JSON reçue depuis le client.
     */
    public static final class IncomingMessage implements Message {
        public final JsonNode text;
        public IncomingMessage(JsonNode text) { this.text = text; }
    }

    /**
     * Message indiquant que la connexion WebSocket a été fermée.
     */
    public static final class ConnectionClosed implements Message {
        public static final ConnectionClosed INSTANCE = new ConnectionClosed();
        private ConnectionClosed() {}
    }

    /**
     * Message used when a client reconnects.
     */
    public static final class ReconnectMessage implements Message {
        private org.apache.pekko.actor.ActorRef ws;
        public ReconnectMessage(org.apache.pekko.actor.ActorRef ws) {
            this.ws = ws;
        }
    }

    /**
     * Base class for messages sent from the server to the client.
     */
    public static abstract class OutputMessage implements Message {
        protected final JsonNode payload;
        protected OutputMessage(JsonNode payload) {
            this.payload = payload;
        }
    }

    /** Message containing fight information. */
    public static final class SendFight extends OutputMessage {
        public SendFight(JsonNode fight){
            super(fight);
        }
    }

    /** Message announcing the start of a new round. */
    public static final class StartOfRound extends OutputMessage {
        public StartOfRound(JsonNode payload) {
            super(payload);
        }
    }

    /** Message containing feedback on a player's action. */
    public static final class FeedbackInput extends OutputMessage{
        public FeedbackInput(JsonNode payload) {
            super(payload);
        }
    }

    /** Message containing updates from the opponent. */
    public static final class ChangesFromOtherUser extends OutputMessage {
        public ChangesFromOtherUser(JsonNode payload) {super(payload);
        }
    }

    /** Message announcing the end of the game. */
    public static final class EndGame extends OutputMessage{
        public EndGame(JsonNode winner){
            super(winner);
        }
    }

    /** Message sent during game initialization. */
    public static final class SetupMessage extends OutputMessage{
        public SetupMessage(JsonNode payload){super(payload);}
    }


    /**
     * Internal message used to verify that the connection is still active.
     */
    public static final class Heartbeat implements Message {
        public static final Heartbeat INSTANCE = new Heartbeat();
        private Heartbeat() {}
    }

    /**
     * Message requesting the actor to stop cleanly after the reconnection timeout expires.
     */
    public static final class GracefulStop implements Message {
        public static final GracefulStop INSTANCE = new GracefulStop();
        private GracefulStop() {}
    }


    /**
     * Message indicating which {@link GameActor} this connection is associated with.
     */
    public static final class StartGame implements Message{
        public final ActorRef<GameActor.Message> game;
        public StartGame(ActorRef<GameActor.Message> game){
            this.game = game;
        }
    }

    private org.apache.pekko.actor.ActorRef ws;
    private ActorRef<GameActor.Message> game;
    private final MatchmakingService matchmakingService;
    public long userId;

    private List<JsonNode> reconnectionBuffer = new ArrayList<>();
    private List<IncomingMessage> endOfRoundBuffer = new ArrayList<>();

    private Cancellable heartbeat;
    private Cancellable reconnectTimer;
    private int missedHeartbeats = 0;
    private static final int MAX_MISSED = 3;

    private final ActeurMonitor monitor;

    private long endOfRoundTime = 0;


    /**
     * Creates a new {@code ConnexionActor}.
     *
     * @param ws WebSocket associated with the player
     * @param userId player identifier
     * @param monitor actor monitor
     * @param matchmakingService matchmaking service
     * @return the actor's initial behavior
     */
    public static Behavior<Message> create(org.apache.pekko.actor.ActorRef ws, long userId, ActeurMonitor monitor, MatchmakingService matchmakingService) {
        return Behaviors.setup(ctx -> new ConnexionActor(ctx, ws, userId, monitor, matchmakingService));
    }

    /**
     * Initializes a connection actor.
     */
    private ConnexionActor(ActorContext<Message> ctx, org.apache.pekko.actor.ActorRef ws, long userId, ActeurMonitor monitor, MatchmakingService matchmakingService) {
        super(ctx);
        this.ws = ws;
        this.userId = userId;
        this.monitor = monitor;
        this.matchmakingService = matchmakingService;
        startHeartbeat(ctx);
    }

    /**
     * Defines the messages handled by the actor.
     *
     * @return the receive behavior for messages
     */
    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(IncomingMessage.class, this::onIncoming)
                .onMessage(ConnectionClosed.class, this::onConnectionClosed)
                .onMessage(ReconnectMessage.class, this::onReconnectMessage)
                .onMessage(Heartbeat.class, this::onHeartbeat)
                .onMessage(GracefulStop.class, this::onGracefulStop)
                .onMessage(SendFight.class, this::onOutputMessage)
                .onMessage(FeedbackInput.class, this::onOutputMessage)
                .onMessage(ChangesFromOtherUser.class, this::onOutputMessage)
                .onMessage(EndGame.class, this::onOutputMessage)
                .onMessage(SetupMessage.class, this::onOutputMessage)
                .onMessage(StartOfRound.class, this::onStartingRound)
                .onMessage(StartGame.class, this::onStartGame)
                .build();
    }

    /**
     * Handles a message received from the client and routes it to the
     * appropriate component (matchmaking, game, reconnection...).
     *
     * @param msg received message
     * @return the next behavior
     */
    private Behavior<Message> onIncoming(IncomingMessage msg) {
        // process messages once the game is available
        if (msg.text.get(TIME).longValue() >= endOfRoundTime){
            endOfRoundBuffer.add(msg);
        }

        switch (msg.text.get(TYPE).asText()){
            case HISTORIQUE -> {
                //TODO
            }
            case JOUER -> {
                long messageId = msg.text.get(ID).longValue();
                matchmakingService.addPlayer(String.valueOf(userId));
            }
            case BUY -> game.tell(new GameActor.BuyingUnitMessage(userId, msg.text.get(PAYLOAD).get(UNIT).longValue(), msg.text.get(ID).longValue(), getContext().getSelf()));
            case SELL -> game.tell(new GameActor.SellingUnitMessage(userId, msg.text.get(PAYLOAD).get(UNIT).longValue(), msg.text.get(ID).longValue(), getContext().getSelf()));
            case MOVE -> {
                int x = msg.text.get(PAYLOAD).get(POSITION).get("x").intValue();
                int y = msg.text.get(PAYLOAD).get(POSITION).get("y").intValue();
                long unitId = msg.text.get(PAYLOAD).get(UNIT).longValue();
                long messageId = msg.text.get(ID).longValue();
                game.tell(new GameActor.MovingUnitMessage(userId, messageId, unitId, new Tuple(x,y), getContext().getSelf() ));
            }
            case GIVE -> {
                long unitId = msg.text.get(PAYLOAD).get(UNIT).longValue();
                long itemId = msg.text.get(PAYLOAD).get(ITEM).longValue();
                long messageId = msg.text.get(ID).longValue();
                game.tell(new GameActor.GivingUnitObjectMessage(userId, messageId ,unitId, itemId, getContext().getSelf()));
            }
            case REROLL -> game.tell(new GameActor.RerollShopMessage(userId, msg.text.get(ID).longValue(), getContext().getSelf()));
            case EXP -> game.tell(new GameActor.BuyingExpMessage(userId, msg.text.get(ID).longValue(), getContext().getSelf()));
            case ACK -> {
                long id = msg.text.get(ID).longValue();
                reconnectionBuffer.removeIf(node -> node.get(ID).longValue() == id);
            }
            case RECO -> {
                if (ws != null) {
                    for (JsonNode node : reconnectionBuffer){
                        ws.tell(node, org.apache.pekko.actor.ActorRef.noSender());
                    }
                }
            }
            case PONG -> missedHeartbeats = 0;
            default -> ws.tell(Json.newObject().put(TYPE, ERROR).put(LOG , "unknown type found"),
                    org.apache.pekko.actor.ActorRef.noSender());
        }
        return Behaviors.same();
    }

    /**
     * Handles the closing of the WebSocket connection.
     * Starts a grace period allowing for reconnection.
     *
     * @param msg close notification
     * @return the next behavior
     */
    private Behavior<Message> onConnectionClosed(ConnectionClosed msg) {
        this.ws = null;
        heartbeat.cancel();

        reconnectTimer = getContext().scheduleOnce(
                Duration.ofMinutes(5),
                getContext().getSelf(),
                GracefulStop.INSTANCE
        );

        return Behaviors.same();
    }

    /**
     * Associates this actor with the {@link GameActor} managing the game.
     *
     * @param msg message containing the game actor
     * @return the next behavior
     */
    private Behavior<Message> onStartGame(StartGame msg){
        game = msg.game;
        game.tell(new GameActor.ConnexionSetupMessage(this.userId, getContext().getSelf()));
        return Behaviors.same();
    }

    /**
     * Sends a message to the client and stores it in the reconnection buffer.
     *
     * @param msg message to send
     * @return the next behavior
     */
    private Behavior<Message> onOutputMessage(OutputMessage msg) {
        reconnectionBuffer.add(msg.payload);
        if (ws != null) {
            ws.tell(msg.payload,  org.apache.pekko.actor.ActorRef.noSender());
        }
        return Behaviors.same();
    }


    /**
     * Handles the start of a new round and replays any actions
     * buffered during the transition.
     *
     * @param msg start-of-round message
     * @return the next behavior
     */
    private Behavior<Message> onStartingRound(StartOfRound msg) {
        reconnectionBuffer.add(msg.payload);
        if (ws != null) {
            endOfRoundTime = msg.payload.get(PAYLOAD).longValue();

            ws.tell(msg.payload,  org.apache.pekko.actor.ActorRef.noSender());
        }
        for (IncomingMessage bufferedMsg: endOfRoundBuffer){
            ObjectNode json = (ObjectNode) bufferedMsg.text;
            json.put(TIME, System.currentTimeMillis());
            getContext().getSelf().tell(new IncomingMessage(json));
        }
        return Behaviors.same();
    }


    /**
     * Periodically checks that the client still responds.
     * Disconnects the player automatically after several
     * heartbeats without response.
     *
     * @param msg heartbeat message
     * @return the next behavior
     */
    private Behavior<Message> onHeartbeat(Heartbeat msg) {
        if (ws == null) return Behaviors.same();

        missedHeartbeats++;
        if (missedHeartbeats >= MAX_MISSED) {
            return onConnectionClosed(ConnectionClosed.INSTANCE);
        }

        ws.tell(Json.newObject().put(TYPE, PING),
                org.apache.pekko.actor.ActorRef.noSender());
        return Behaviors.same();
    }

    /**
     * Restores the WebSocket connection after a reconnection.
     *
     * @param msg reconnection information
     * @return the next behavior
     */
    private Behavior<Message> onReconnectMessage(ReconnectMessage msg) {
        this.ws = msg.ws;
        missedHeartbeats = 0;

        if (reconnectTimer != null) {
            reconnectTimer.cancel();
            reconnectTimer = null;
        }

        startHeartbeat(getContext());
        return Behaviors.same();
    }

    /**
     * Stops the actor gracefully following a reconnection timeout.
     *
     * @param msg graceful stop request
     * @return the next behavior
     */
    private Behavior<Message> onGracefulStop(GracefulStop msg) {
        heartbeat.cancel();
        monitor.removeByActor(getContext().getSelf());
        return Behaviors.stopped();
    }

    /**
     * Starts the periodic heartbeat task.
     *
     * @param ctx actor context
     */
    private void startHeartbeat(ActorContext<Message> ctx) {
        heartbeat = ctx.getSystem().scheduler().scheduleWithFixedDelay(
                Duration.ofSeconds(30),
                Duration.ofSeconds(30),
                () -> ctx.getSelf().tell(Heartbeat.INSTANCE),
                ctx.getExecutionContext()
        );
    }

}
