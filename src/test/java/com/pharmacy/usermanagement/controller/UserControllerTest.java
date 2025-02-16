package com.pharmacy.usermanagement.controller;

import com.pharmacy.usermanagement.dto.request.RegisterRequest;
import com.pharmacy.usermanagement.dto.request.UpdateUserRequest;
import com.pharmacy.usermanagement.dto.response.ApiResponse;
import com.pharmacy.usermanagement.dto.response.UserResponseDto;
import com.pharmacy.usermanagement.model.UserEntity;
import com.pharmacy.usermanagement.repository.UserRepository;
import com.pharmacy.usermanagement.service.UserService;
import com.pharmacy.usermanagement.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ Test: Get all users (Success)
    @Test
    void testGetAllUsers_Success() {
        UserEntity user1 = new UserEntity();
        user1.setUserId(1L);
        user1.setEmail("user1@example.com");

        UserEntity user2 = new UserEntity();
        user2.setUserId(2L);
        user2.setEmail("user2@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userService.mapToUserResponseDto(any(UserEntity.class)))
                .thenReturn(new UserResponseDto(
                        1L, "user1@example.com", "hashedpassword123",
                        "User", "One", "Address", "123456789",
                        true, List.of(1L, 2L)
                ));

        ResponseEntity<ApiResponse<List<UserResponseDto>>> response = userController.getAllUsers();

        assertNotNull(response);
        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().size());
    }

    // ✅ Test: Get user by ID (Success)
    @Test
    void testGetUserById_Success() {
        UserEntity user = new UserEntity();
        user.setUserId(1L);
        user.setEmail("user1@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userService.mapToUserResponseDto(user))
                .thenReturn(new UserResponseDto(
                        1L, "user1@example.com", "hashedpassword123",
                        "User", "One", "Address", "123456789",
                        true, List.of(1L, 2L)
                ));

        ResponseEntity<ApiResponse<UserResponseDto>> response = userController.getUserById(1L);

        assertNotNull(response);
        assertTrue(response.getBody().isSuccess());
        assertEquals("user1@example.com", response.getBody().getData().getEmail());
    }

    // ❌ Test: Get user by ID (User Not Found)
    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userController.getUserById(99L);
        });

        assertEquals("User not found", exception.getMessage());
    }

    // ✅ Test: Register a new user (Success)
    @Test
    void testRegisterUser_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("securePass123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setAddress("123 Street");
        request.setMobileNumber("9876543210");
        request.setActive(true);

        UserEntity savedUser = new UserEntity();
        savedUser.setUserId(1L);
        savedUser.setEmail(request.getEmail());

        when(userService.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(userService.mapToUserResponseDto(savedUser))
                .thenReturn(new UserResponseDto(
                        1L, "newuser@example.com", "hashedpassword123",
                        "John", "Doe", "123 Street", "9876543210",
                        true, List.of()
                ));

        ResponseEntity<ApiResponse<UserResponseDto>> response = userController.registerUser(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("User registered successfully!", response.getBody().getMessage());
    }

    // ❌ Test: Register a user (Email Already Exists)
    @Test
    void testRegisterUser_EmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(userService.existsByEmail(request.getEmail())).thenReturn(true);

        ResponseEntity<ApiResponse<UserResponseDto>> response = userController.registerUser(request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Email is already in use!", response.getBody().getMessage());
    }

    // ✅ Test: Update user (Success)
    @Test
    void testUpdateUser_Success() {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("User");
        updateRequest.setAddress("New Address");
        updateRequest.setMobileNumber("9999999999");
        updateRequest.setActive(false);

        UserEntity existingUser = new UserEntity();
        existingUser.setUserId(1L);
        existingUser.setEmail("user@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userService.mapToUserResponseDto(existingUser))
                .thenReturn(new UserResponseDto(
                        1L, "user@example.com", "hashedpassword123",
                        "Updated", "User", "New Address", "9999999999",
                        false, List.of()
                ));

        ResponseEntity<ApiResponse<UserResponseDto>> response = userController.updateUser(1L, updateRequest);

        assertNotNull(response);
        assertTrue(response.getBody().isSuccess());
        assertEquals("User updated successfully", response.getBody().getMessage());
    }

    // ✅ Test: Delete user (Success)
    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        ResponseEntity<ApiResponse<Void>> response = userController.deleteUser(1L);

        assertNotNull(response);
        assertTrue(response.getBody().isSuccess());
        assertEquals("User deleted successfully", response.getBody().getMessage());
    }

    // ❌ Test: Delete user (User Not Found)
    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        ResponseEntity<ApiResponse<Void>> response = userController.deleteUser(99L);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found", response.getBody().getMessage());
    }
}
