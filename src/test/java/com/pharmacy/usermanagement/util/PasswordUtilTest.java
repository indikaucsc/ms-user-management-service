package com.pharmacy.usermanagement.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    // ✅ Test: Hash Password
    @Test
    void testHashPassword() {
        String password = "SecurePass123";
        String hashedPassword = PasswordUtil.hashPassword(password);
        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
    }

    // ✅ Test: Verify Password (Success)
    @Test
    void testVerifyPassword_Success() {
        String password = "SecurePass123";
        String hashedPassword = PasswordUtil.hashPassword(password);
        assertTrue(PasswordUtil.verifyPassword(password, hashedPassword));
    }

    // ❌ Test: Verify Password (Failure)
    @Test
    void testVerifyPassword_Failure() {
        String password = "SecurePass123";
        String wrongPassword = "WrongPass123";
        String hashedPassword = PasswordUtil.hashPassword(password);
        assertFalse(PasswordUtil.verifyPassword(wrongPassword, hashedPassword));
    }

    // ✅ Test: Ensure consistent password hashing (same password hashes differently)
    @Test
    void testHashPassword_Consistency() {
        String password = "SecurePass123";
        String hashedPassword1 = PasswordUtil.hashPassword(password);
        String hashedPassword2 = PasswordUtil.hashPassword(password);

        assertNotEquals(hashedPassword1, hashedPassword2); // Hashing should be unique due to salt
    }
}
