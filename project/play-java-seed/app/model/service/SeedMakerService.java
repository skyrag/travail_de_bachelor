package model.service;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Singleton
public class SeedMakerService {

    private Random generator;

    public SeedMakerService(){
        generator = new Random();
    }

    public Long createGameSeed(){
        return generator.nextLong();
    }

    public Long createFromSeed(long baseSeed, String context){
        try {
            String combined = baseSeed + ":" + context;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(hash).getLong(); // prend les 8 premiers bytes
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 est garanti disponible sur toute JVM standard, ne devrait jamais arriver
            throw new RuntimeException(e);
        }
    }

}
