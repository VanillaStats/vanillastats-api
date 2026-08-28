package net.vanillastats.api.apikeys.service;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class ApiKeyGenerator {
    private static final String PREFIX = "vs_live_";
    private final SecureRandom secureRandom = new SecureRandom();

    public record GeneratedKey(String rawKey, String hash, String prefix) {
    }

    public GeneratedKey generate() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        String rawKey = PREFIX + randomPart;
        String hash = hash(rawKey);
        String prefix = rawKey.substring(0, Math.min(12, rawKey.length()));

        return new GeneratedKey(rawKey, hash, prefix);
    }

    public String hash(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawKey.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
