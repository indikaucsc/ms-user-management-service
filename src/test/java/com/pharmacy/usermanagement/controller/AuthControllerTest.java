package com.pharmacy.usermanagement.controller;

import com.pharmacy.usermanagement.dto.request.LoginRequest;
import com.pharmacy.usermanagement.dto.response.LoginResponse;
import com.pharmacy.usermanagement.service.UserService;
import com.pharmacy.usermanagement.util.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ Test: Successful Authentication
    @Test
    void testAuthenticateUser_Success() {
        // Mock request
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@example.com");

        // Mock JWT token generation
        when(userService.getRoleIdByEmail("user@example.com")).thenReturn(1L);
        when(jwtTokenUtil.generateToken(anyString(), anyLong()))
                .thenReturn("mocked_jwt_token");

        // Call the API method
        ResponseEntity<LoginResponse> response = authController.authenticateUser(request);

        // Assertions
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals("mocked_jwt_token", response.getBody().getToken());
    }

    // ❌ Test: Authentication Failure (Invalid Credentials)
    @Test
    void testAuthenticateUser_Failure_InvalidCredentials() {
        // Mock request
        LoginRequest request = new LoginRequest();
        request.setEmail("wronguser@example.com");
        request.setPassword("wrongpassword");

        // Simulate authentication failure
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        // Call API and expect exception
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authController.authenticateUser(request);
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }
}
