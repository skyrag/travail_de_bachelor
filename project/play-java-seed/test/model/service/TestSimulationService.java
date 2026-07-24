package model.service;

import model.DTO.fighting.FightingResultDTO;
import model.entities.Team;
import model.service.fightingService.FightingService;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.dispatch.Dispatchers;
import org.apache.pekko.dispatch.MessageDispatcher;
import org.junit.Test;
import org.mockito.MockedConstruction;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NOTE: adaptez si besoin les imports/types Pekko (Dispatchers,
 * MessageDispatcher) à la version exacte utilisée par votre projet. L'idée
 * est simplement de forcer le dispatcher renvoyé par
 * actorSystem.dispatchers().lookup("combat-simulation-dispatcher") à
 * exécuter les tâches de façon synchrone (dans le thread de test), afin de
 * pouvoir vérifier le résultat sans dépendre d'un vrai pool de threads.
 */
public class TestSimulationService {

    @Test
    public void simulateAsync_runsFightingServiceOnConfiguredExecutorAndReturnsResult()
            throws ExecutionException, InterruptedException, TimeoutException {

        ActorSystem actorSystem = mock(ActorSystem.class);
        Dispatchers dispatchers = mock(Dispatchers.class);
        MessageDispatcher dispatcher = mock(MessageDispatcher.class);

        when(actorSystem.dispatchers()).thenReturn(dispatchers);
        when(dispatchers.lookup("combat-simulation-dispatcher")).thenReturn(dispatcher);
        // Force l'exécution synchrone des tâches soumises au dispatcher mocké.
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(dispatcher).execute(any(Runnable.class));

        Team teamA = mock(Team.class);
        Team teamB = mock(Team.class);
        FightingResultDTO expected = mock(FightingResultDTO.class);

        try (MockedConstruction<FightingService> mockedConstruction = mockConstruction(
                FightingService.class,
                (mockEngine, context) -> {
                    assertEquals(3, context.arguments().size());
                    assertEquals(123L, context.arguments().get(0));
                    assertSame(teamA, context.arguments().get(1));
                    assertSame(teamB, context.arguments().get(2));
                    when(mockEngine.simulate()).thenReturn(expected);
                })) {

            SimulationService service = new SimulationService(actorSystem);
            CompletionStage<FightingResultDTO> stage = service.simulateAsync(teamA, teamB, 123L);

            FightingResultDTO result = stage.toCompletableFuture().get(5, TimeUnit.SECONDS);

            assertSame(expected, result);
            assertEquals(1, mockedConstruction.constructed().size());
            verify(mockedConstruction.constructed().get(0)).simulate();
        }

        verify(dispatchers).lookup("combat-simulation-dispatcher");
    }
}