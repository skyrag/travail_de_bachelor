package model.monitor;

import model.actor.ConnexionActor;
import model.actor.GameActor;
import model.entities.game.Game;
import model.repositories.GameCreationRepository;
import model.repositories.GameRepository;
import model.service.GameLevelService;
import model.service.MatchmakingService;
import model.service.SeedMakerService;
import model.service.SimulationService;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.Adapter;
import org.apache.pekko.japi.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Nécessite Mockito 5+ (mock statique intégré via mockStatic) ou l'artefact
 * mockito-inline pour les versions antérieures, car Adapter.spawn(...) est
 * une méthode statique de Pekko qu'on ne veut pas exécuter pour de vrai
 * (elle démarrerait un véritable acteur).
 */
public class TestActeurMonitor {

    private ActorSystem actorSystem;
    private GameRepository gameRepo;
    private GameCreationRepository creationRepo;
    private MatchmakingService matchmakingService;
    private SeedMakerService seedGenerator;
    private GameLevelService gameLevelService;
    private SimulationService simulationService;

    private ActeurMonitor monitor;

    @Before
    public void setUp() {
        actorSystem = mock(ActorSystem.class);
        gameRepo = mock(GameRepository.class);
        creationRepo = mock(GameCreationRepository.class);
        matchmakingService = mock(MatchmakingService.class);
        seedGenerator = mock(SeedMakerService.class);
        gameLevelService = mock(GameLevelService.class);
        simulationService = mock(SimulationService.class);

        monitor = new ActeurMonitor(actorSystem, gameRepo, seedGenerator, creationRepo,
                gameLevelService, simulationService, matchmakingService);
    }

    @SuppressWarnings("unchecked")
    private static <T> ActorRef<T> mockActorRef() {
        return mock(ActorRef.class);
    }

    @Test
    public void getOrCreateActorFromId_createsAndRegistersNewActorForUnknownUser() {
        org.apache.pekko.actor.ActorRef ws = mock(org.apache.pekko.actor.ActorRef.class);
        ActorRef<ConnexionActor.Message> spawned = mockActorRef();

        try (MockedStatic<Adapter> adapter = mockStatic(Adapter.class)) {
            adapter.when(() -> Adapter.spawn(eq(actorSystem), any(), eq("42")))
                    .thenReturn(spawned);

            ActorRef<ConnexionActor.Message> result = monitor.getOrCreateActorFromId("42", ws);

            assertSame(spawned, result);
            adapter.verify(() -> Adapter.spawn(eq(actorSystem), any(), eq("42")), times(1));
        }
    }

    @Test
    public void getOrCreateActorFromId_returnsSameActorForKnownUserWithoutSpawningAgain() {
        org.apache.pekko.actor.ActorRef ws = mock(org.apache.pekko.actor.ActorRef.class);
        ActorRef<ConnexionActor.Message> spawned = mockActorRef();

        try (MockedStatic<Adapter> adapter = mockStatic(Adapter.class)) {
            adapter.when(() -> Adapter.spawn(eq(actorSystem), any(), eq("42")))
                    .thenReturn(spawned);

            ActorRef<ConnexionActor.Message> first = monitor.getOrCreateActorFromId("42", ws);
            ActorRef<ConnexionActor.Message> second = monitor.getOrCreateActorFromId("42", ws);

            assertSame(first, second);
            adapter.verify(() -> Adapter.spawn(eq(actorSystem), any(), eq("42")), times(1));
        }
    }

    @Test
    public void getActorFromId_returnsPreviouslyCreatedActor() {
        org.apache.pekko.actor.ActorRef ws = mock(org.apache.pekko.actor.ActorRef.class);
        ActorRef<ConnexionActor.Message> spawned = mockActorRef();

        try (MockedStatic<Adapter> adapter = mockStatic(Adapter.class)) {
            adapter.when(() -> Adapter.spawn(eq(actorSystem), any(), eq("42")))
                    .thenReturn(spawned);

            monitor.getOrCreateActorFromId("42", ws);
            ActorRef<ConnexionActor.Message> found = monitor.getActorFromId("42");

            assertSame(spawned, found);
        }
    }

    @Test
    public void getActorFromId_throwsForUnknownUser() {
        // Comportement actuel : aucune vérification -> indexOf renvoie -1 et
        // connexionList.get(-1) lève une IndexOutOfBoundsException.
        assertThrows(IndexOutOfBoundsException.class, () -> monitor.getActorFromId("unknown"));
    }

    @Test
    public void removeByActor_allowsUserToBeRecreatedAfterwards() {
        org.apache.pekko.actor.ActorRef ws = mock(org.apache.pekko.actor.ActorRef.class);
        ActorRef<ConnexionActor.Message> firstActor = mockActorRef();
        ActorRef<ConnexionActor.Message> secondActor = mockActorRef();

        try (MockedStatic<Adapter> adapter = mockStatic(Adapter.class)) {
            adapter.when(() -> Adapter.spawn(eq(actorSystem), any(), eq("42")))
                    .thenReturn(firstActor)
                    .thenReturn(secondActor);

            ActorRef<ConnexionActor.Message> created = monitor.getOrCreateActorFromId("42", ws);
            assertSame(firstActor, created);

            monitor.removeByActor(created);

            ActorRef<ConnexionActor.Message> recreated = monitor.getOrCreateActorFromId("42", ws);
            assertSame(secondActor, recreated);
            adapter.verify(() -> Adapter.spawn(eq(actorSystem), any(), eq("42")), times(2));
        }
    }

    @Test
    public void removeByActor_throwsForActorThatWasNeverRegistered() {
        ActorRef<ConnexionActor.Message> unknownActor = mockActorRef();

        // Comportement actuel : indexOf renvoie -1 et idList.remove(-1)
        // lève une IndexOutOfBoundsException.
        assertThrows(IndexOutOfBoundsException.class, () -> monitor.removeByActor(unknownActor));
    }

    @Test
    public void createGame_callsRepositoriesAndSpawnsGameActor() {
        List<Pair<ActorRef<ConnexionActor.Message>, Long>> players = List.of();
        List<Long> playerIds = List.of(1L, 2L);
        String version = "1.0";

        Game game = mock(Game.class);
        ActorRef<GameActor.Message> spawnedGameActor = mockActorRef();

        when(creationRepo.createGame(eq(version), any(), eq(playerIds)))
                .thenReturn(CompletableFuture.completedFuture(game));

        try (MockedStatic<Adapter> adapter = mockStatic(Adapter.class)) {
            adapter.when(() -> Adapter.spawn(eq(actorSystem), any(), any()))
                    .thenReturn(spawnedGameActor);

            ActorRef<GameActor.Message> result = monitor.createGame(players, version, playerIds);

            assertSame(spawnedGameActor, result);
            verify(creationRepo).createGame(eq(version), any(), eq(playerIds));
            verify(seedGenerator).createGameSeed();
            adapter.verify(() -> Adapter.spawn(eq(actorSystem), any(), any()), times(1));
        }
    }
}