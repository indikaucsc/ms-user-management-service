package com.pharmacy.usermanagement.service;

import com.pharmacy.usermanagement.dto.request.RegisterRequest;
import com.pharmacy.usermanagement.dto.response.UserResponseDto;
import com.pharmacy.usermanagement.model.RoleEntity;
import com.pharmacy.usermanagement.model.UserEntity;
import com.pharmacy.usermanagement.model.UserRoleEntity;
import com.pharmacy.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ Test: Check if email exists (Email Found)
    @Test
    void testExistsByEmail_Found() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new UserEntity()));
        assertTrue(userService.existsByEmail("user@example.com"));
    }

    // ❌ Test: Check if email exists (Email Not Found)
    @Test
    void testExistsByEmail_NotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertFalse(userService.existsByEmail("notfound@example.com"));
    }

    // ✅ Test: Get Role ID by Email (Success)
    @Test
    void testGetRoleIdByEmail_Success() {
        UserEntity user = new UserEntity();
        RoleEntity role = new RoleEntity();
        role.setRoleId(1L);
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(user);
        userRole.setRole(role);
        user.setUserRoles(List.of(userRole));

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        Long roleId = userService.getRoleIdByEmail("user@example.com");

        assertNotNull(roleId);
        assertEquals(1L, roleId);
    }

    // ❌ Test: Get Role ID by Email (User Not Found)
    @Test
    void testGetRoleIdByEmail_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getRoleIdByEmail("notfound@example.com");
        });

        assertEquals("User with email notfound@example.com not found", exception.getMessage());
    }

    // ❌ Test: Get Role ID by Email (No Roles Found)
    @Test
    void testGetRoleIdByEmail_NoRolesFound() {
        UserEntity user = new UserEntity();
        user.setUserRoles(List.of()); // No roles assigned

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getRoleIdByEmail("user@example.com");
        });

        assertEquals("No roles found for user with email user@example.com", exception.getMessage());
    }

    // ✅ Test: Map `UserEntity` to `UserResponseDto`
    @Test
    void testMapToUserResponseDto() {
        UserEntity user = new UserEntity();
        user.setUserId(1L);
        user.setEmail("user@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAddress("123 Street");
        user.setMobileNumber("9876543210");
        user.setAccountLocked(false);

        RoleEntity role = new RoleEntity();
        role.setRoleId(2L);
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(user);
        userRole.setRole(role);
        user.setUserRoles(List.of(userRole));

        UserResponseDto dto = userService.mapToUserResponseDto(user);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("user@example.com", dto.getEmail());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("123 Street", dto.getAddress());
        assertEquals("9876543210", dto.getMobileNumber());
        assertFalse(dto.isActive());
        assertEquals(List.of(2L), dto.getRoleIds());
    }

    // ✅ Test: Validate `RegisterRequest` (Success)
    @Test
    void testValidateRegisterRequest_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword("SecurePass123");
        request.setFirstName("John");
        request.setMobileNumber("+123456789012");
        request.setRoleIds(List.of(1L));

        assertDoesNotThrow(() -> userService.validateRegisterRequest(request));
    }

    // ❌ Test: Validate `RegisterRequest` (Invalid Email)
    @Test
    void testValidateRegisterRequest_InvalidEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalidemail");
        request.setPassword("ValidPass123");
        request.setFirstName("John");
        request.setMobileNumber("+123456789012");
        request.setRoleIds(List.of(1L));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.validateRegisterRequest(request);
        });

        assertEquals("Invalid email format", exception.getMessage());
    }

    // ❌ Test: Validate `RegisterRequest` (Short Password)
    @Test
    void testValidateRegisterRequest_ShortPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword("123");
        request.setFirstName("John");
        request.setMobileNumber("+123456789012");
        request.setRoleIds(List.of(1L));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.validateRegisterRequest(request);
        });

        assertEquals("Password must be between 8 and 18 characters long", exception.getMessage());
    }

    // ❌ Test: Validate `RegisterRequest` (Invalid Mobile Number)
    @Test
    void testValidateRegisterRequest_InvalidMobileNumber() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword("SecurePass123");
        request.setFirstName("John");
        request.setMobileNumber("abc12345");
        request.setRoleIds(List.of(1L));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.validateRegisterRequest(request);
        });

        assertEquals("Invalid mobile number format", exception.getMessage());
    }

    // ❌ Test: Validate `RegisterRequest` (Invalid Role ID)
    @Test
    void testValidateRegisterRequest_InvalidRoleId() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword("SecurePass123");
        request.setFirstName("John");
        request.setMobileNumber("+123456789012");
        request.setRoleIds(List.of(99L)); // Invalid Role ID

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.validateRegisterRequest(request);
        });

        assertEquals("Role IDs must be 1 (Admin), 2 (Pharmacist), or 3 (Store Manager)", exception.getMessage());
    }


    // ❌ Test: Role IDs are null
    @Test
    void testValidateRegisterRequest_NullRoleIds() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword("SecurePass123");
        request.setFirstName("John");
        request.setMobileNumber("+123456789012");
        request.setRoleIds(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.validateRegisterRequest(request);
        });

        assertEquals("At least one role ID is required", exception.getMessage());
    }


    // ❌ Test: First name is blank
    @Test
    void testValidateRegisterRequest_BlankFirstName() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword("ValidPass123");
        request.setFirstName("");
        request.setMobileNumber("+123456789012");
        request.setRoleIds(List.of(1L));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.validateRegisterRequest(request);
        });

        assertEquals("First name is required.", exception.getMessage());
    }


    // ❌ Test: Password is null
    @Test
    void testValidateRegisterRequest_NullPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword(null);
        request.setFirstName("John");
        request.setMobileNumber("+123456789012");
        request.setRoleIds(List.of(1L));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.validateRegisterRequest(request);
        });

        assertEquals("Password is required", exception.getMessage());
    }

    // ❌ Test: Email is null
    @Test
    void testValidateRegisterRequest_NullEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(null);
        request.setPassword("ValidPass123");
        request.setFirstName("John");
        request.setMobileNumber("+123456789012");
        request.setRoleIds(List.of(1L));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.validateRegisterRequest(request);
        });

        assertEquals("Email is required", exception.getMessage());
    }

    // ❌ Test: Email is blank
    @Test
    void testValidateRegisterRequest_BlankEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("");
        request.setPassword("ValidPass123");
        request.setFirstName("John");
        request.setMobileNumber("+123456789012");
        request.setRoleIds(List.of(1L));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.validateRegisterRequest(request);
        });

        assertEquals("Email is required", exception.getMessage());
    }

    // ❌ Test: First name exceeds 50 characters
    @Test
    void testValidateRegisterRequest_LongFirstName() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword("ValidPass123");
        request.setFirstName("ThisIsAVeryLongFirstNameThatExceedsFiftyCharacterssdfsfsfsdfsdfsfsdfs");
        request.setMobileNumber("+123456789012");
        request.setRoleIds(List.of(1L));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.validateRegisterRequest(request);
        });

        assertEquals("First name should not exceed 50 characters", exception.getMessage());
    }

    // ❌ Test: First name exceeds 50 characters
    @Test
    void testValidateRegisterRequest_MobileNumber_Null() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword("ValidPass123");
        request.setFirstName("This");
        request.setMobileNumber(null);
        request.setRoleIds(List.of(1L));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.validateRegisterRequest(request);
        });

        assertEquals("Mobile number is required.", exception.getMessage());

    }

    // ❌ Test: First name exceeds 50 characters
    @Test
    void testValidateRegisterRequest_MobileNumber_Balnk() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword("ValidPass123");
        request.setFirstName("This");
        request.setRoleIds(List.of(1L));
        request.setMobileNumber("");


        Exception exceptionBlank = assertThrows(IllegalArgumentException.class, () -> {
            userService.validateRegisterRequest(request);
        });

        assertEquals("Mobile number is required.", exceptionBlank.getMessage());
    }
}
