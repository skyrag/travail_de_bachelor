package model.service;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This class of test tests the fonctionnalities of our hashservice that
 * gives us a way to hash password and verify
 * if a password is the one that is hashed (argon2)
 */
public class TestHashService {

    @Test
    public void testBasicHash () {

        Config config = ConfigFactory.parseString("""
            argon2.iterations = 2
            argon2.memoryKb = 19456
            argon2.parallelism = 1
        """);

        HashService service = new HashService(config);


        String hash = service.hash("password".toCharArray());

        assertNotNull(hash);
        assertTrue(service.verify(hash, "password".toCharArray()));
    }
}
