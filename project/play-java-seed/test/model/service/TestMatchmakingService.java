package model.service;

import com.typesafe.config.Config;
import model.actor.ConnexionActor;
import model.actor.GameActor;
import model.monitor.ActeurMonitor;
import org.apache.pekko.actor.typed.ActorRef;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NOTE: la branche "if (players.isEmpty())" dans addPlayer est du code
 * mort : elle est protégée par "waitingPlayers.size() >= 8" juste avant,
 * donc la boucle "for i in 0..8" garantit toujours au moins 8 éléments
 * pollés (en environnement mono-thread, ce qui est le cas ici puisque la
 * méthode est "synchronized"). Il n'est donc pas possible de couvrir cette
 * branche par un test unitaire classique sans modifier le code de
 * production (ou sans une astuce de réflexion très fragile). Cela devrait
 * être signalé à l'auteur du code comme un branch mort à supprimer,
 * plutôt que testé.
 */
public class TestMatchmakingService {

    private ActeurMonitor monitor;
    private Config config;
    private MatchmakingService matchmakingService;

    @SuppressWarnings("unchecked")
    private ActorRef<ConnexionActor.Message> mockConnexionRef() {
        return mock(ActorRef.class);
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        monitor = mock(ActeurMonitor.class);
        config = mock(Config.class);
        when(config.getString("version")).thenReturn("1.0.0");

        for (long i = 1; i <= 20; i++) {
            when(monitor.getActorFromId(String.valueOf(i))).thenReturn(mockConnexionRef());
        }

        matchmakingService = new MatchmakingService(config);
        matchmakingService.setMonitor(monitor);
    }

    @Test
    public void addPlayer_belowThreshold_returnsNullAndDoesNotCreateGame() {
        for (int i = 1; i <= 7; i++) {
            ActorRef<GameActor.Message> result = matchmakingService.addPlayer(String.valueOf(i));
            assertNull(result);
        }

        verify(monitor, never()).createGame(anyList(), anyString(), anyList());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addPlayer_reachingEightPlayers_createsGameWithConfiguredVersion() {
        ActorRef<GameActor.Message> gameRef = mock(ActorRef.class);
        when(monitor.createGame(anyList(), eq("1.0.0"), anyList())).thenReturn(gameRef);

        ActorRef<GameActor.Message> result = null;
        for (int i = 1; i <= 8; i++) {
            result = matchmakingService.addPlayer(String.valueOf(i));
        }

        assertSame(gameRef, result);
        verify(monitor, times(1)).createGame(anyList(), eq("1.0.0"), anyList());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addPlayer_queueIsResetAfterGameCreation() {
        ActorRef<GameActor.Message> gameRef = mock(ActorRef.class);
        when(monitor.createGame(anyList(), eq("1.0.0"), anyList())).thenReturn(gameRef);

        for (int i = 1; i <= 8; i++) {
            matchmakingService.addPlayer(String.valueOf(i));
        }

        // La file d'attente doit être vide après la création de partie :
        // 7 joueurs supplémentaires ne doivent pas déclencher une 2e partie.
        for (int i = 9; i <= 15; i++) {
            ActorRef<GameActor.Message> result = matchmakingService.addPlayer(String.valueOf(i));
            assertNull(result);
        }

        verify(monitor, times(1)).createGame(anyList(), eq("1.0.0"), anyList());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addPlayer_passesExactPlayerIdsInOrderToCreateGame() {
        ActorRef<GameActor.Message> gameRef = mock(ActorRef.class);
        when(monitor.createGame(anyList(), eq("1.0.0"), anyList())).thenReturn(gameRef);

        for (int i = 1; i <= 8; i++) {
            matchmakingService.addPlayer(String.valueOf(i));
        }

        verify(monitor).createGame(anyList(), eq("1.0.0"),
                eq(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addPlayer_twoConsecutiveMatchesAreCreatedIndependently() {
        ActorRef<GameActor.Message> gameRef1 = mock(ActorRef.class);
        ActorRef<GameActor.Message> gameRef2 = mock(ActorRef.class);
        when(monitor.createGame(anyList(), eq("1.0.0"), anyList()))
                .thenReturn(gameRef1)
                .thenReturn(gameRef2);

        ActorRef<GameActor.Message> firstResult = null;
        for (int i = 1; i <= 8; i++) {
            firstResult = matchmakingService.addPlayer(String.valueOf(i));
        }

        ActorRef<GameActor.Message> secondResult = null;
        for (int i = 9; i <= 16; i++) {
            secondResult = matchmakingService.addPlayer(String.valueOf(i));
        }

        assertSame(gameRef1, firstResult);
        assertSame(gameRef2, secondResult);
        verify(monitor, times(2)).createGame(anyList(), eq("1.0.0"), anyList());
    }
}