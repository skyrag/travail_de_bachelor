package model.service;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Service responsible for password hashing and verification using
 * the Argon2id algorithm.
 * <p>
 * The hashing parameters (iterations, memory usage and parallelism)
 * are loaded from the application configuration and can be adjusted
 * depending on the deployment environment.
 * <p>
 * For security reasons, password character arrays are wiped from
 * memory after use.
 */
@Singleton
public class HashService {

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    // parameters that should change depending on server
    private final int ITERATIONS;
    private final int MEMORY_KB;
    private final int PARALLELISM;

    @Inject
    public HashService(com.typesafe.config.Config config){
        ITERATIONS = config.getInt("argon2.iterations");
        MEMORY_KB = config.getInt("argon2.memoryKb");
        PARALLELISM = config.getInt("argon2.parallelism");
    }

    /**
     * Generates a secure Argon2id hash for the provided password.
     * <p>
     * The password array is wiped from memory after hashing.
     *
     * @param password the password to hash
     * @return the generated Argon2id hash
     */
    public String hash(char[] password) {
        try {
            return argon2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, password);
        } finally {
            argon2.wipeArray(password); // efface le mot de passe de la mémoire
        }
    }

    /**
     * Verifies whether a password matches a previously generated hash.
     * <p>
     * The password array is wiped from memory after verification.
     *
     * @param hash the stored Argon2id hash
     * @param password the password to verify
     * @return true if the password matches the hash,
     *         false otherwise
     */
    public boolean verify(String hash, char[] password) {
        try {
            return argon2.verify(hash, password);
        } finally {
            argon2.wipeArray(password);
        }
    }
}
