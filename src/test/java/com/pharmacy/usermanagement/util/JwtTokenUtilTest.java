package com.pharmacy.usermanagement.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    @InjectMocks
    private JwtTokenUtil jwtTokenUtil;

    private final String secret = "mySuperSecretKeyThatNeedsToBeAtLeast32CharactersLong";
    private final long expiration = 600; // 10 minutes
    private String generatedToken;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Inject the secret key using reflection
        Field secretField = JwtTokenUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtTokenUtil, secret);

        // Inject the expiration value using reflection
        Field expirationField = JwtTokenUtil.class.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtTokenUtil, expiration);

        // Generate a test token
        generatedToken = jwtTokenUtil.generateToken("testuser", 1L);
    }

    // ✅ Test: Generate Token
    @Test
    void testGenerateToken() {
        String token = jwtTokenUtil.generateToken("testuser", 1L);
        assertNotNull(token);
    }

    // ✅ Test: Validate Token (Success)
    @Test
    void testValidateToken_Success() {
        assertTrue(jwtTokenUtil.validateToken(generatedToken));
    }

    // ❌ Test: Validate Token (Expired)
    @Test
    void testValidateToken_Expired() throws Exception {
        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 60000)) // 1 min ago
                .setExpiration(new Date(System.currentTimeMillis() - 30000)) // Expired 30 sec ago
                .signWith(getSigningKeyFromReflection(), SignatureAlgorithm.HS256) // Use reflection
                .compact();

        assertFalse(jwtTokenUtil.validateToken(expiredToken));
    }

    // ✅ Test: Extract Username from Token
    @Test
    void testGetUsernameFromToken() {
        assertEquals("testuser", jwtTokenUtil.getUsernameFromToken(generatedToken));
    }

    // ✅ Test: Extract Role ID from Token (Ensure Integer return type)
    @Test
    void testGetRoleIdFromToken() {
        assertEquals(1, jwtTokenUtil.getRoleIdFromToken(generatedToken));
    }

    // ✅ Test: Check Token Expiration
    @Test
    void testIsTokenExpired() {
        assertFalse(jwtTokenUtil.isTokenExpired(generatedToken));
    }

    // ✅ Test: Get Expiration Date from Token
    @Test
    void testGetExpirationDateFromToken() {
        Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(generatedToken);
        assertNotNull(expirationDate);
    }

    // ✅ Reflection method to access private `getSigningKey()`
    private Key getSigningKeyFromReflection() throws Exception {
        Method method = JwtTokenUtil.class.getDeclaredMethod("getSigningKey");
        method.setAccessible(true); // Allow access to private method
        return (Key) method.invoke(jwtTokenUtil);
    }
}
