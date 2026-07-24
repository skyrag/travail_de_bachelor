package model.service;

import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Ces tests appellent la véritable librairie Argon2 (native, via JNI).
 * On utilise des paramètres volontairement faibles (peu d'itérations, peu
 * de mémoire) uniquement pour accélérer les tests ; ne jamais utiliser ces
 * valeurs en production.
 */
public class TestHashService {

    private HashService hashService;

    @Before
    public void setUp() {
        Config config = mock(Config.class);
        when(config.getInt("argon2.iterations")).thenReturn(1);
        when(config.getInt("argon2.memoryKb")).thenReturn(4096);
        when(config.getInt("argon2.parallelism")).thenReturn(1);

        hashService = new HashService(config);
    }

    @Test
    public void hash_producesNonNullArgon2idHash() {
        char[] password = "SuperSecret123!".toCharArray();

        String hash = hashService.hash(password);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$argon2id$"));
    }

    @Test
    public void hash_wipesPasswordArrayAfterHashing() {
        char[] password = "SuperSecret123!".toCharArray();

        hashService.hash(password);

        assertArrayEquals(new char[password.length], password);
    }

    @Test
    public void verify_correctPassword_returnsTrue() {
        char[] original = "SuperSecret123!".toCharArray();
        String hash = hashService.hash(original);

        char[] toVerify = "SuperSecret123!".toCharArray();
        boolean result = hashService.verify(hash, toVerify);

        assertTrue(result);
    }

    @Test
    public void verify_wrongPassword_returnsFalse() {
        char[] original = "SuperSecret123!".toCharArray();
        String hash = hashService.hash(original);

        char[] wrong = "WrongPassword!".toCharArray();
        boolean result = hashService.verify(hash, wrong);

        assertFalse(result);
    }

    @Test
    public void verify_wipesPasswordArrayAfterVerification() {
        char[] original = "SuperSecret123!".toCharArray();
        String hash = hashService.hash(original);

        char[] toVerify = "SuperSecret123!".toCharArray();
        hashService.verify(hash, toVerify);

        assertArrayEquals(new char[toVerify.length], toVerify);
    }
}