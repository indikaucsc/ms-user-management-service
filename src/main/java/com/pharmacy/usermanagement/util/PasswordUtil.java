package com.pharmacy.usermanagement.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordUtil {

    private static final int SALT_LENGTH = 16; // Length of the salt
    private static final int HASH_ITERATIONS = 10000; // Number of iterations
    private static final int KEY_LENGTH = 256; // Length of the hash in bits

    // Method to hash a password
    public static String hashPassword(String password) {
//        try {
//            // Generate a random salt
//            byte[] salt = generateSalt();
//            // Hash the password
//            byte[] hashedPassword = hashPasswordWithSalt(password, salt);
//            // Encode salt and hash in Base64 for storage
//            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hashedPassword);
//        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
//            throw new RuntimeException("Error while hashing password", e);
//        }
        return BCrypt.hashpw(password,BCrypt.gensalt());
    }

    // Method to verify a password
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Split stored hash into salt and hash
            String[] parts = storedHash.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);
            // Hash the provided password with the stored salt
            byte[] hashedPassword = hashPasswordWithSalt(password, salt);
            // Compare the hashes
            return java.util.Arrays.equals(hashedPassword, hash);
        } catch (Exception e) {
            throw new RuntimeException("Error while verifying password", e);
        }
    }

    // Generate a random salt
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    // Hash a password with the given salt
    private static byte[] hashPasswordWithSalt(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, HASH_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        return keyFactory.generateSecret(spec).getEncoded();
    }
}

