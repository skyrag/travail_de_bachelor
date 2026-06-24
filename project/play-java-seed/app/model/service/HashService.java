package model.service;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import javax.inject.Inject;
import javax.inject.Singleton;

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

    public String hash(char[] password) {
        try {
            return argon2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, password);
        } finally {
            argon2.wipeArray(password); // efface le mot de passe de la mémoire
        }
    }

    public boolean verify(String hash, char[] password) {
        try {
            return argon2.verify(hash, password);
        } finally {
            argon2.wipeArray(password);
        }
    }
}
