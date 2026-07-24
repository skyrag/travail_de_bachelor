package model.actor;

import com.fasterxml.jackson.databind.JsonNode;
import model.entities.Team;
import model.entities.game.Game;
import model.entities.unit.Unit;
import model.repositories.GameRepository;
import model.service.GameLevelService;
import model.service.SeedMakerService;
import model.service.SimulationService;
import model.utils.Tuple;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.japi.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static model.actor.JsonConstantes.*;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IMPORTANT — hypothèses faites faute d'avoir le code de Game/Team/GameRepository
 * /SeedMakerService/GameLevelService sous les yeux (uniquement déduites de la façon
 * dont GameActor les appelle) :
 *  - Game : getTeams(), getPools(), getSeed()->long, getTeam(long)->Team,
 *           getUnitById(long)->Unit, canRemoveUnitToPool(long)->boolean,
 *           canAddUnitToPool(long)->boolean
 *  - Team : canBuyUnit(Unit), canSellUnit(long), canMoveUnit(Tuple,long),
 *           canAddItemToUnit(long,long), getLvl()->int, canBuyExp(int)
 *  - GameRepository : merge(Game)/merge(Team) -> CompletionStage<?>, getAllItems()
 *  - GameLevelService : getExpRequired(int)->int
 *
 * Ce fichier laisse volontairement de côté onSetupMessage (boucle sur les équipes),
 * onRerollShopMessage et onEndOfRoundMessage/onEndOfFightMessage : ces méthodes
 * dépendent de Team.getSeed() (type ambigu dans le code fourni — tantôt utilisé comme
 * un long, tantôt comme un Random via .nextInt(100)) et de FightingResultDTO /
 * Fight / Round dont je n'ai pas les constructeurs exacts. Partage-moi ces classes
 * si tu veux que je complète la couverture sur ces branches.
 */
public class TestGameActor {

    private static ActorTestKit testKit;

    private Game game;
    private GameRepository repo;
    private SeedMakerService seedGenerator;
    private GameLevelService gameLevelService;
    private SimulationService simulationService;

    @BeforeAll
    static void initSystem() {
        testKit = ActorTestKit.create();
    }

    @AfterAll
    static void shutdownSystem() {
        testKit.shutdownTestKit();
    }

    @BeforeEach
    void setUp() {
        game = mock(Game.class);
        repo = mock(GameRepository.class);
        seedGenerator = mock(SeedMakerService.class);
        gameLevelService = mock(GameLevelService.class);
        simulationService = mock(SimulationService.class);

        // teams/pools vides par défaut : évite de dépendre de Team.getSeed()
        // (voir la note en tête de fichier) dans le onSetupMessage déclenché
        // automatiquement à la construction de l'acteur.
        when(game.getTeams()).thenReturn(new ArrayList<>());
        when(game.getPools()).thenReturn(new ArrayList<>());
        when(game.getSeed()).thenReturn(1L);

        when(repo.merge(any(Game.class))).thenReturn(CompletableFuture.completedFuture(null));
        when(repo.merge(any(Team.class))).thenReturn(CompletableFuture.completedFuture(null));
    }

    private ActorRef<GameActor.Message> spawn(List<Pair<ActorRef<ConnexionActor.Message>, Long>> users) {
        return testKit.spawn(GameActor.create(users, game, repo, seedGenerator, gameLevelService, simulationService));
    }

    private JsonNode payloadOf(ConnexionActor.Message msg) {
        return ((ConnexionActor.OutputMessage) msg).payload;
    }

    // ---- Construction ----

    @Test
    void constructor_sendsStartGameToAllUsersAndMergesGame() {
        TestProbe<ConnexionActor.Message> probe1 = testKit.createTestProbe(ConnexionActor.Message.class);
        TestProbe<ConnexionActor.Message> probe2 = testKit.createTestProbe(ConnexionActor.Message.class);
        List<Pair<ActorRef<ConnexionActor.Message>, Long>> users = List.of(
                Pair.create(probe1.getRef(), 1L),
                Pair.create(probe2.getRef(), 2L)
        );

        spawn(users);

        probe1.expectMessageClass(ConnexionActor.StartGame.class);
        probe2.expectMessageClass(ConnexionActor.StartGame.class);
        verify(repo, timeout(1000)).merge(game);
    }

    // ---- onConnexionSetupMessage ----

    @Test
    void onConnexionSetupMessage_teamNotFound_sendsNothing() {
        when(game.getTeam(5L)).thenReturn(null);
        ActorRef<GameActor.Message> actor = spawn(List.of());

        TestProbe<ConnexionActor.Message> respondTo = testKit.createTestProbe(ConnexionActor.Message.class);
        actor.tell(new GameActor.ConnexionSetupMessage(5L, respondTo.getRef()));

        respondTo.expectNoMessage(Duration.ofMillis(500));
    }

    @Test
    void onConnexionSetupMessage_teamFound_sendsSetupMessage() {
        Team team = mock(Team.class);
        when(game.getTeam(5L)).thenReturn(team);
        when(repo.getAllItems()).thenReturn(CompletableFuture.completedFuture(List.of()));

        ActorRef<GameActor.Message> actor = spawn(List.of());

        TestProbe<ConnexionActor.Message> respondTo = testKit.createTestProbe(ConnexionActor.Message.class);
        actor.tell(new GameActor.ConnexionSetupMessage(5L, respondTo.getRef()));

        ConnexionActor.Message received = respondTo.receiveMessage();
        assertInstanceOf(ConnexionActor.SetupMessage.class, received);
        assertInstanceOf(String.class, payloadOf(received).get(TYPE).asText());
        org.junit.jupiter.api.Assertions.assertEquals(SETUP, payloadOf(received).get(TYPE).asText());
    }

    // ---- onBuyingUnitMessage ----

    @Test
    void onBuyingUnitMessage_teamNotFound_sendsError() {
        when(game.getTeam(1L)).thenReturn(null);
        ActorRef<GameActor.Message> actor = spawn(List.of());
        TestProbe<ConnexionActor.Message> respondTo = testKit.createTestProbe(ConnexionActor.Message.class);

        actor.tell(new GameActor.BuyingUnitMessage(1L, 7L, 10L, respondTo.getRef()));

        org.junit.jupiter.api.Assertions.assertEquals(ERROR, payloadOf(respondTo.receiveMessage()).get(TYPE).asText());
    }



    // ---- onSellUnitMessage ----

    @Test
    void onSellUnitMessage_cannotSell_sendsError() {
        Team team = mock(Team.class);
        when(game.getTeam(1L)).thenReturn(team);
        when(team.canSellUnit(7L)).thenReturn(false);

        ActorRef<GameActor.Message> actor = spawn(List.of());
        TestProbe<ConnexionActor.Message> respondTo = testKit.createTestProbe(ConnexionActor.Message.class);

        actor.tell(new GameActor.SellingUnitMessage(1L, 7L, 10L, respondTo.getRef()));

        org.junit.jupiter.api.Assertions.assertEquals(ERROR, payloadOf(respondTo.receiveMessage()).get(TYPE).asText());
    }

    // ---- onGivingUnitObjectMessage ----

    @Test
    void onGivingUnitObjectMessage_success_mergesTeam() {
        Team team = mock(Team.class);
        when(game.getTeam(1L)).thenReturn(team);
        when(team.canAddItemToUnit(9L, 7L)).thenReturn(true);

        TestProbe<ConnexionActor.Message> probe = testKit.createTestProbe(ConnexionActor.Message.class);
        ActorRef<GameActor.Message> actor = spawn(List.of(Pair.create(probe.getRef(), 1L)));
        probe.expectMessageClass(ConnexionActor.StartGame.class);

        actor.tell(new GameActor.GivingUnitObjectMessage(1L, 10L, 7L, 9L, probe.getRef()));

        org.junit.jupiter.api.Assertions.assertEquals(OK, payloadOf(probe.receiveMessage()).get(TYPE).asText());
        verify(repo, timeout(1000)).merge(team);
    }

    // ---- onBuyingExpMessage ----

    @Test
    void onBuyingExpMessage_cannotAfford_sendsError() {
        Team team = mock(Team.class);
        when(game.getTeam(1L)).thenReturn(team);
        when(team.getLvl()).thenReturn(1);
        when(gameLevelService.getExpRequired(1)).thenReturn(100);
        when(team.canBuyExp(100)).thenReturn(false);

        ActorRef<GameActor.Message> actor = spawn(List.of());
        TestProbe<ConnexionActor.Message> respondTo = testKit.createTestProbe(ConnexionActor.Message.class);

        actor.tell(new GameActor.BuyingExpMessage(1L, 10L, respondTo.getRef()));

        org.junit.jupiter.api.Assertions.assertEquals(ERROR, payloadOf(respondTo.receiveMessage()).get(TYPE).asText());
    }

    @Test
    void onBuyingExpMessage_success_confirmsAndMergesTeam() {
        Team team = mock(Team.class);
        when(game.getTeam(1L)).thenReturn(team);
        when(team.getLvl()).thenReturn(1);
        when(gameLevelService.getExpRequired(1)).thenReturn(100);
        when(team.canBuyExp(100)).thenReturn(true);

        TestProbe<ConnexionActor.Message> probe = testKit.createTestProbe(ConnexionActor.Message.class);
        ActorRef<GameActor.Message> actor = spawn(List.of(Pair.create(probe.getRef(), 1L)));
        probe.expectMessageClass(ConnexionActor.StartGame.class);

        actor.tell(new GameActor.BuyingExpMessage(1L, 10L, probe.getRef()));

        org.junit.jupiter.api.Assertions.assertEquals(OK, payloadOf(probe.receiveMessage()).get(TYPE).asText());
        verify(repo, timeout(1000)).merge(team);
    }

    // ---- onStartOfRoundMessage ----

    @Test
    void onStartOfRoundMessage_sendsStartOfRoundToAllUsers() {
        TestProbe<ConnexionActor.Message> probe1 = testKit.createTestProbe(ConnexionActor.Message.class);
        TestProbe<ConnexionActor.Message> probe2 = testKit.createTestProbe(ConnexionActor.Message.class);
        ActorRef<GameActor.Message> actor = spawn(List.of(
                Pair.create(probe1.getRef(), 1L),
                Pair.create(probe2.getRef(), 2L)
        ));
        probe1.expectMessageClass(ConnexionActor.StartGame.class);
        probe2.expectMessageClass(ConnexionActor.StartGame.class);

        actor.tell(GameActor.StartOfRoundMessage.INSTANCE);

        probe1.expectMessageClass(ConnexionActor.StartOfRound.class);
        probe2.expectMessageClass(ConnexionActor.StartOfRound.class);
    }

    // ---- onEndOfGame ----

    @Test
    void onEndOfGame_stopsActor() {
        ActorRef<GameActor.Message> actor = spawn(List.of());
        TestProbe<GameActor.Message> watcher = testKit.createTestProbe(GameActor.Message.class);

        actor.tell(GameActor.EndOfGame.INSTANCE);

        watcher.expectTerminated(actor, Duration.ofSeconds(3));
    }
}