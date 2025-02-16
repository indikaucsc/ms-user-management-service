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

        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Method to verify a password
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            return BCrypt.checkpw(password, storedHash); // ✅ Uses correct BCrypt verification
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

