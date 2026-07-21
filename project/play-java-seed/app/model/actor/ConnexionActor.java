package model.actor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.monitor.ActeurMonitor;
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
 * The websocket communicate using json that has this form :
 On va traduire ici a quoi va ressembler un paylod généré par notre utilisateur lors des ses inputs ainsi que les différents check qu'il est nécessaire de réaliser pour s'assurer qu'aucune triche n'a lieu. Un Id sera présent dans le paylod pour que l'on puisse buffer les actions afin de revert en cas de triche.

 Paylod classique :

 ```json
 {
 "id" : "",
 "type": "",
 "paylod": {
 "entity1" : "",
 "entity2" : ""
 }
 }
 ```

 pour les types ils sont la pour décrire les différentes action possible de l'utilisateur voici les move où le paylod est l'unité affecter:
 - "BuyUnit"
 - "SellUnit"
 - "MoveUnit" : ici le paylod contiens en premier lieu(entity1) l'unité affecter et en deuxième lieux (entity2) la nouvelle positions (x, y)
 - "GiveToUnit" : ici le paylod contiens en premier lieu(entity1) l'unité affecter et en deuxième lieux (entity2) l'object affecté

 Ensuite il y a le reste des type :
 - "FuseObject" : ici le paylod contiens les deux objects a fusionner
 - "RerollShop" : ici le paylod est vide (sera ignoré)
 - "BuyExp" : ici le paylod est vide (sera ignoré)

 Maintenant en ce qui concerne les check a faire pour s'assurer de la non triche :
 - "BuyUnit" :
 - Frontend : check que l'on a assez d'argent pour le faire
 - Backend :
 - check si l'unité est présente dans notre shop actuellement
 - check si le user a assez d'argent pour acheté l'unité
 - check si le user a la place pour acheté l'unité
 - "SellUnit" :
 - check si le user a l'unité
 - "MoveUnit"
 - check si le déplacement est dans la partie du user de l'arène (frontend aussi)
 - check si le user a cette unité
 - check si le user peut ajouté l'unité a son équipe si elle n'en fait pas déjà parti (frontend aussi)
 - "GiveToUnit" :
 - check si l'on possède l'unité
 - check si l'on possède l'object dans l'inventaire
 - check si l'unité a pas déjà 3 objects (complété ?)
 - checker le type d'ôbject (remover change les règles a checker)
 - "FuseObject" :
 - check qu'on a bien les deux objects a disposition
 - check que c'est deux objects qui peuvent être fuse
 - "RerollShop" :
 - check que le user a bien l'argent pour roll (frontend aussi)
 - "BuyExp" :
 - check que le user a bien l'argent pour acheter de l'exp
 - check que le user n'est pas niveau maximum

 Enfin pour les messages serveur -> client il y aura aussi les type :
 - "Error" : qui traduit que le move "id" est illegal
 - "OK" : qui traduit que le move "id" est légal
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

    public static final class ReconnectMessage implements Message {
        private org.apache.pekko.actor.ActorRef ws;
        public ReconnectMessage(org.apache.pekko.actor.ActorRef ws) {
            this.ws = ws;
        }
    }

    public static abstract class OutputMessage implements Message {
        protected final JsonNode payload;
        protected OutputMessage(JsonNode payload) {
            this.payload = payload;
        }
    }

    public static final class SendFight extends OutputMessage {
        public SendFight(JsonNode fight){
            super(fight);
        }
    }

    public static final class StartOfRound extends OutputMessage {
        public StartOfRound(JsonNode payload) {
            super(payload);
        }
    }

    public static final class FeedbackInput extends OutputMessage{
        public FeedbackInput(JsonNode payload) {
            super(payload);
        }
    }

    public static final class ChangesFromOtherUser extends OutputMessage {
        public ChangesFromOtherUser(JsonNode payload) {super(payload);
        }
    }

    public static final class EndGame extends OutputMessage{
        public EndGame(JsonNode winner){
            super(winner);
        }
    }

    public static final class SetupMessage extends OutputMessage{
        public SetupMessage(JsonNode payload){super(payload);}
    }

    public static final class Heartbeat implements Message {
        public static final Heartbeat INSTANCE = new Heartbeat();
        private Heartbeat() {}
    }

    public static final class GracefulStop implements Message {
        public static final GracefulStop INSTANCE = new GracefulStop();
        private GracefulStop() {}
    }

    public static final class StartGame implements Message{
        public final ActorRef<GameActor.Message> game;
        public StartGame(ActorRef<GameActor.Message> game){
            this.game = game;
        }
    }

    private org.apache.pekko.actor.ActorRef ws;
    private ActorRef<GameActor.Message> game;
    public long userId;

    private List<JsonNode> reconnectionBuffer = new ArrayList<>();
    private List<IncomingMessage> endOfRoundBuffer = new ArrayList<>();

    private Cancellable heartbeat;
    private Cancellable reconnectTimer;
    private int missedHeartbeats = 0;
    private static final int MAX_MISSED = 3;

    private final ActeurMonitor monitor;

    private long endOfRoundTime = 0;

    public static Behavior<Message> create(org.apache.pekko.actor.ActorRef ws, long userId, ActeurMonitor monitor) {
        return Behaviors.setup(ctx -> new ConnexionActor(ctx, ws, userId, monitor));
    }

    private ConnexionActor(ActorContext<Message> ctx, org.apache.pekko.actor.ActorRef ws, long userId, ActeurMonitor monitor) {
        super(ctx);
        this.ws = ws;
        this.userId = userId;
        this.monitor = monitor;
        startHeartbeat(ctx);
    }

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

    private Behavior<Message> onIncoming(IncomingMessage msg) {
        // faire un traitement des message une fois qu'on a la game
        if (msg.text.get(TIME).longValue() >= endOfRoundTime){
            endOfRoundBuffer.add(msg);
        }

        switch (msg.text.get(TYPE).asText()){
            case BUY -> game.tell(new GameActor.BuyingUnitMessage(userId, msg.text.get(PAYLOAD).get(UNIT).longValue(), msg.text.get(ID).longValue(), getContext().getSelf()));
            case SELL -> game.tell(new GameActor.SellingUnitMessage(userId, msg.text.get(PAYLOAD).get(UNIT).longValue(), msg.text.get(ID).longValue(), getContext().getSelf()));
            case MOVE -> {
                int x = msg.text.get(PAYLOAD).get(POSITION).get("x").intValue();
                int y = msg.text.get(PAYLOAD).get(POSITION).get("y").intValue();
                long unitId = msg.text.get(PAYLOAD).get(POSITION).longValue();
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

    private Behavior<Message> onStartGame(StartGame msg){
        game = msg.game;
        game.tell(new GameActor.ConnexionSetupMessage(this.userId, getContext().getSelf()));
        return Behaviors.same();
    }

    private Behavior<Message> onOutputMessage(OutputMessage msg) {
        reconnectionBuffer.add(msg.payload);
        if (ws != null) {
            ws.tell(msg.payload,  org.apache.pekko.actor.ActorRef.noSender());
        }
        return Behaviors.same();
    }

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

    private Behavior<Message> onGracefulStop(GracefulStop msg) {
        heartbeat.cancel();
        monitor.removeByActor(getContext().getSelf());
        return Behaviors.stopped();
    }

    private void startHeartbeat(ActorContext<Message> ctx) {
        heartbeat = ctx.getSystem().scheduler().scheduleWithFixedDelay(
                Duration.ofSeconds(30),
                Duration.ofSeconds(30),
                () -> ctx.getSelf().tell(Heartbeat.INSTANCE),
                ctx.getExecutionContext()
        );
    }

}
