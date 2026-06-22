package com.fleurairlines.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

public class PasswordUtil {
    private static final int SALT_LENGTH = 16;
    private static final String HASH_ALGORITHM = "SHA-256";
   // Change this pattern to include # 
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%?&])[A-Za-z\\d!@#$%?&]{8,}$"
);

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String salt) throws DatabaseException {
        try {
            String combined = password + salt;
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(combined.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new DatabaseException("Error hashing password", e);
        }
    }

    public static boolean verify(String rawPassword, String salt, String storedHash) throws DatabaseException {
        try {
            String computedHash = hash(rawPassword, salt);
            return computedHash.equals(storedHash);
        } catch (DatabaseException e) {
            throw e;
        }
    }

    public static boolean isStrongPassword(String password) {
        return password != null && STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    public static String getPasswordRequirements(String password) {
        if (password == null || password.isBlank()) {
            return "Password cannot be empty.";
        }
        if (password.length() < 8) {
            return "❌ Password must be at least 8 characters long.";
        }
        if (!password.matches(".*[a-z].*")) {
            return "❌ Password must contain at least one lowercase letter (a-z).";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "❌ Password must contain at least one uppercase letter (A-Z).";
        }
        if (!password.matches(".*\\d.*")) {
            return "❌ Password must contain at least one digit (0-9).";
        }
        if (!password.matches(".*[!@#$%?&].*")) {
            return "❌ Password must contain at least one special character (!@#$%?&).";
        }
        return "✅ Password is strong!";
    }
}
