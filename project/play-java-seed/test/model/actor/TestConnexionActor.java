package model.actor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.monitor.ActeurMonitor;
import model.service.MatchmakingService;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.libs.Json;

import static model.actor.JsonConstantes.*;
import static org.mockito.Mockito.*;

/**
 * Utilise un vrai système d'acteurs de test (ActorTestKit) plutôt qu'un
 * BehaviorTestKit synchrone, car ConnexionActor démarre un heartbeat via
 * ctx.getSystem().scheduler() dans son constructeur.
 *
 * Les constantes de JsonConstantes (TYPE, ID, TIME, PAYLOAD, UNIT, ITEM,
 * POSITION, BUY, SELL, MOVE, GIVE, REROLL, EXP, JOUER, ACK, RECO, PONG,
 * ERROR, PING, OK...) sont importées directement : ce fichier n'a donc pas
 * besoin de connaître leur valeur textuelle réelle.
 */
public class TestConnexionActor {

    private static ActorTestKit testKit;

    private MatchmakingService matchmakingService;
    private ActeurMonitor monitor;
    private org.apache.pekko.actor.ActorRef ws;

    @BeforeClass
    public static void initSystem() {
        testKit = ActorTestKit.create();
    }

    @AfterClass
    public static void shutdownSystem() {
        testKit.shutdownTestKit();
    }

    @Before
    public void setUp() {
        matchmakingService = mock(MatchmakingService.class);
        monitor = mock(ActeurMonitor.class);
        ws = mock(org.apache.pekko.actor.ActorRef.class);
    }

    private ActorRef<ConnexionActor.Message> spawnActor(long userId) {
        return testKit.spawn(ConnexionActor.create(ws, userId, monitor, matchmakingService));
    }

    private static ObjectNode base(String type, long id, long time) {
        return Json.newObject().put(TYPE, type).put(ID, id).put(TIME, time);
    }

    // ---- onStartGame ----

    @Test
    public void onStartGame_forwardsConnexionSetupToGameActor() {
        TestProbe<GameActor.Message> gameProbe = testKit.createTestProbe(GameActor.Message.class);
        ActorRef<ConnexionActor.Message> actor = spawnActor(42L);

        actor.tell(new ConnexionActor.StartGame(gameProbe.getRef()));

        gameProbe.expectMessageClass(GameActor.ConnexionSetupMessage.class);
    }

    // ---- onIncoming : forwarding vers GameActor ----

    @Test
    public void onIncoming_buy_forwardsBuyingUnitMessageToGame() {
        TestProbe<GameActor.Message> gameProbe = testKit.createTestProbe(GameActor.Message.class);
        ActorRef<ConnexionActor.Message> actor = spawnActor(42L);
        actor.tell(new ConnexionActor.StartGame(gameProbe.getRef()));
        gameProbe.expectMessageClass(GameActor.ConnexionSetupMessage.class);

        ObjectNode msg = base(BUY, 2, 0);
        msg.set(PAYLOAD, Json.newObject().put(UNIT, 7));
        actor.tell(new ConnexionActor.IncomingMessage(msg));

        gameProbe.expectMessageClass(GameActor.BuyingUnitMessage.class);
    }

    @Test
    public void onIncoming_move_forwardsMovingUnitMessageToGame() {
        TestProbe<GameActor.Message> gameProbe = testKit.createTestProbe(GameActor.Message.class);
        ActorRef<ConnexionActor.Message> actor = spawnActor(42L);
        actor.tell(new ConnexionActor.StartGame(gameProbe.getRef()));
        gameProbe.expectMessageClass(GameActor.ConnexionSetupMessage.class);

        ObjectNode msg = base(MOVE, 3, 0);
        msg.set(PAYLOAD, Json.newObject()
                .put(UNIT, 7)
                .set(POSITION, Json.newObject().put("x", 1).put("y", 2)));
        actor.tell(new ConnexionActor.IncomingMessage(msg));

        gameProbe.expectMessageClass(GameActor.MovingUnitMessage.class);
    }

    @Test
    public void onIncoming_give_forwardsGivingUnitObjectMessageToGame() {
        TestProbe<GameActor.Message> gameProbe = testKit.createTestProbe(GameActor.Message.class);
        ActorRef<ConnexionActor.Message> actor = spawnActor(42L);
        actor.tell(new ConnexionActor.StartGame(gameProbe.getRef()));
        gameProbe.expectMessageClass(GameActor.ConnexionSetupMessage.class);

        ObjectNode msg = base(GIVE, 4, 0);
        msg.set(PAYLOAD, Json.newObject().put(UNIT, 7).put(ITEM, 9));
        actor.tell(new ConnexionActor.IncomingMessage(msg));

        gameProbe.expectMessageClass(GameActor.GivingUnitObjectMessage.class);
    }

    @Test
    public void onIncoming_reroll_forwardsRerollShopMessageToGame() {
        TestProbe<GameActor.Message> gameProbe = testKit.createTestProbe(GameActor.Message.class);
        ActorRef<ConnexionActor.Message> actor = spawnActor(42L);
        actor.tell(new ConnexionActor.StartGame(gameProbe.getRef()));
        gameProbe.expectMessageClass(GameActor.ConnexionSetupMessage.class);

        actor.tell(new ConnexionActor.IncomingMessage(base(REROLL, 5, 0)));

        gameProbe.expectMessageClass(GameActor.RerollShopMessage.class);
    }

    @Test
    public void onIncoming_exp_forwardsBuyingExpMessageToGame() {
        TestProbe<GameActor.Message> gameProbe = testKit.createTestProbe(GameActor.Message.class);
        ActorRef<ConnexionActor.Message> actor = spawnActor(42L);
        actor.tell(new ConnexionActor.StartGame(gameProbe.getRef()));
        gameProbe.expectMessageClass(GameActor.ConnexionSetupMessage.class);

        actor.tell(new ConnexionActor.IncomingMessage(base(EXP, 6, 0)));

        gameProbe.expectMessageClass(GameActor.BuyingExpMessage.class);
    }

    @Test
    public void onIncoming_unknownType_sendsErrorToWs() {
        ActorRef<ConnexionActor.Message> actor = spawnActor(42L);

        actor.tell(new ConnexionActor.IncomingMessage(base("__not_a_real_type__", 7, 0)));

        verify(ws, timeout(1000)).tell(argThat(arg ->
                arg instanceof JsonNode json && ERROR.equals(json.get(TYPE).asText())), any());
    }

    // ---- Heartbeat ----

    @Test
    public void onHeartbeat_sendsPingWhileBelowMaxMissed() {
        ActorRef<ConnexionActor.Message> actor = spawnActor(42L);

        actor.tell(ConnexionActor.Heartbeat.INSTANCE);
        actor.tell(ConnexionActor.Heartbeat.INSTANCE);

        verify(ws, timeout(1000).times(2)).tell(argThat(arg ->
                arg instanceof JsonNode json && PING.equals(json.get(TYPE).asText())), any());
    }


    @Test
    public void onReconnectMessage_replacesWsAndResumesHeartbeat() {
        ActorRef<ConnexionActor.Message> actor = spawnActor(42L);

        actor.tell(ConnexionActor.Heartbeat.INSTANCE);
        actor.tell(ConnexionActor.Heartbeat.INSTANCE);
        actor.tell(ConnexionActor.Heartbeat.INSTANCE); // ws devient null

        org.apache.pekko.actor.ActorRef ws2 = mock(org.apache.pekko.actor.ActorRef.class);
        actor.tell(new ConnexionActor.ReconnectMessage(ws2));
        actor.tell(ConnexionActor.Heartbeat.INSTANCE);

        verify(ws2, timeout(1000)).tell(argThat(arg ->
                arg instanceof JsonNode json && PING.equals(json.get(TYPE).asText())), any());
    }
    // ---- StartOfRound ----

    @Test
    public void onStartingRound_sendsPayloadToWs() {
        ActorRef<ConnexionActor.Message> actor = spawnActor(42L);

        ObjectNode roundPayload = Json.newObject().put(PAYLOAD, 123456L).put(TYPE, "round");
        actor.tell(new ConnexionActor.StartOfRound(roundPayload));

        verify(ws, timeout(1000)).tell(eq(roundPayload), any());
    }

}