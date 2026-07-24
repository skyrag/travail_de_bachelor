package model.service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

public class TestSeedmakerService {

    @Test
    void createGameSeed_returnsNonNullLong() {
        SeedMakerService service = new SeedMakerService();

        Long seed = service.createGameSeed();

        assertNotNull(seed);
    }

    @Test
    void createGameSeed_returnsDifferentValuesAcrossCalls() {
        SeedMakerService service = new SeedMakerService();

        Long seed1 = service.createGameSeed();
        Long seed2 = service.createGameSeed();

        // collision statistiquement quasi impossible avec Random#nextLong
        assertNotEquals(seed1, seed2);
    }

    @Test
    void createFromSeed_isDeterministicForIdenticalInputs() {
        SeedMakerService service = new SeedMakerService();

        Long result1 = service.createFromSeed(42L, "context");
        Long result2 = service.createFromSeed(42L, "context");

        assertEquals(result1, result2);
    }

    @Test
    void createFromSeed_differsWhenContextDiffers() {
        SeedMakerService service = new SeedMakerService();

        Long result1 = service.createFromSeed(42L, "contextA");
        Long result2 = service.createFromSeed(42L, "contextB");

        assertNotEquals(result1, result2);
    }

    @Test
    void createFromSeed_differsWhenBaseSeedDiffers() {
        SeedMakerService service = new SeedMakerService();

        Long result1 = service.createFromSeed(1L, "context");
        Long result2 = service.createFromSeed(2L, "context");

        assertNotEquals(result1, result2);
    }

    @Test
    void createFromSeed_returnsNonNullValueEvenForEmptyContext() {
        SeedMakerService service = new SeedMakerService();

        assertNotNull(service.createFromSeed(0L, ""));
    }

    /**
     * Couvre le bloc catch(NoSuchAlgorithmException) qui, en conditions
     * réelles, est inatteignable (SHA-256 est garanti disponible sur toute
     * JVM standard). On force artificiellement l'exception via un mock
     * statique de MessageDigest pour obtenir 100% de couverture de branche.
     * Nécessite Mockito avec le mock-maker "inline" (mockito-inline, ou
     * mockito-core >= 5 qui l'active par défaut).
     */
    @Test
    void createFromSeed_wrapsNoSuchAlgorithmExceptionInRuntimeException() {
        SeedMakerService service = new SeedMakerService();

        try (MockedStatic<MessageDigest> mocked = mockStatic(MessageDigest.class)) {
            mocked.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("simulated"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.createFromSeed(1L, "ctx"));

            assertInstanceOf(NoSuchAlgorithmException.class, ex.getCause());
        }
    }
}