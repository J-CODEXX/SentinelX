package com.cybersentinel.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HexFormat;

public final class PasswordUtil {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    /**
     * Generates a random cryptographic salt hex string.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    /**
     * Computes PBKDF2WithHmacSHA256 hash using the provided salt.
     */
    public static String hashPassword(String password, String saltHex) {
        if (password == null || saltHex == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }
        try {
            byte[] salt = HexFormat.of().parseHex(saltHex);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password with PBKDF2", e);
        }
    }

    /**
     * Verifies plain password matches the stored hash with salt.
     */
    public static boolean verifyPassword(String candidatePassword, String storedHash, String storedSalt) {
        if (candidatePassword == null || storedHash == null || storedSalt == null) {
            return false;
        }
        String candidateHash = hashPassword(candidatePassword, storedSalt);
        return candidateHash.equalsIgnoreCase(storedHash);
    }

    /**
     * Validates password strength policy (at least 8 chars, 1 digit, 1 letter).
     */
    public static boolean isValidPolicy(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasDigit = false;
        boolean hasLetter = false;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            if (Character.isLetter(c)) hasLetter = true;
        }
        return hasDigit && hasLetter;
    }
}
