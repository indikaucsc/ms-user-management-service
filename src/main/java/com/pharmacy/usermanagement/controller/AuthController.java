package com.pharmacy.usermanagement.controller;

import com.pharmacy.usermanagement.dto.request.LoginRequest;
import com.pharmacy.usermanagement.dto.request.RegisterRequest;
import com.pharmacy.usermanagement.dto.response.ApiResponse;
import com.pharmacy.usermanagement.dto.response.LoginResponse;
import com.pharmacy.usermanagement.dto.response.UserResponseDto;
import com.pharmacy.usermanagement.model.RoleEntity;
import com.pharmacy.usermanagement.model.UserEntity;
import com.pharmacy.usermanagement.model.UserRoleEntity;
import com.pharmacy.usermanagement.repository.RoleRepository;
import com.pharmacy.usermanagement.repository.UserRepository;
import com.pharmacy.usermanagement.repository.UserRoleRepository;
import com.pharmacy.usermanagement.service.UserService;
import com.pharmacy.usermanagement.util.JwtTokenUtil;
import com.pharmacy.usermanagement.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = new LoginResponse();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));


            String token = jwtTokenUtil.generateToken(authentication.getName(),userService.getRoleIdByEmail(loginRequest.getEmail()));

            loginResponse.setSuccess(true);
            loginResponse.setMessage("Login successful");
            loginResponse.setToken(token);
            loginResponse.setRefreshToken(token);
            return ResponseEntity.ok(loginResponse);
        } catch (BadCredentialsException e) {
            loginResponse.setSuccess(false);
            loginResponse.setMessage("Invalid email or password");
            return ResponseEntity.status(401).body(loginResponse);
        } catch (Exception e) {
            loginResponse.setSuccess(false);
            loginResponse.setMessage("An error occurred while processing your request");
            return ResponseEntity.status(500).body(loginResponse);
        }
    }


    // Utility method to map UserEntity to UserResponseDto
    private UserResponseDto mapToUserResponseDto(UserEntity user) {
        List<Long> roleIds = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleId())
                .collect(Collectors.toList());

        UserResponseDto dto = new UserResponseDto();
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAddress(user.getAddress());
        dto.setMobileNumber(user.getMobileNumber());
        dto.setActive(user.isAccountLocked());
        dto.setRoleIds(roleIds);
        dto.setId(user.getUserId());

        return dto;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(@RequestBody RegisterRequest registerRequest) {
        ApiResponse<UserResponseDto> response = new ApiResponse<>();
        try {
            if (userService.existsByEmail(registerRequest.getEmail())) {
                response.setSuccess(false);
                response.setMessage("Email is already in use!");
                return ResponseEntity.badRequest().body(response);
            }

            String hashedPassword = PasswordUtil.hashPassword(registerRequest.getPassword());
            UserEntity newUser = new UserEntity();
            newUser.setEmail(registerRequest.getEmail());
            newUser.setFirstName(registerRequest.getFirstName());
            newUser.setLastName(registerRequest.getLastName());
            newUser.setPassword(hashedPassword);
            newUser.setAddress(registerRequest.getAddress());
            newUser.setMobileNumber(registerRequest.getMobileNumber());
            newUser.setAccountLocked(registerRequest.isActive());

            UserEntity savedUser = userRepository.save(newUser);

            if (registerRequest.getRoleIds() != null && !registerRequest.getRoleIds().isEmpty()) {
                for (Long roleId : registerRequest.getRoleIds()) {
                    RoleEntity role = roleRepository.findByRoleId(roleId)
                            .orElseThrow(() -> new RuntimeException("Role not found for ID: " + roleId));
                    UserRoleEntity userRole = new UserRoleEntity();
                    userRole.setUser(savedUser);
                    userRole.setRole(role);
                    UserRoleEntity saveUserRoleEntity =  userRoleRepository.save(userRole);
                    savedUser.setUserRoles(List.of(saveUserRoleEntity));
                }
            }

//            UserEntity user = userRepository.findByIdWithRoles(savedUser.getUserId())
//                    .orElseThrow(() -> new RuntimeException("An error occurred while registering the user"));
//
            response.setSuccess(true);
            response.setMessage("User registered successfully!");
            response.setData(mapToUserResponseDto(savedUser));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("An error occurred while registering the user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        ApiResponse<List<UserResponseDto>> response = new ApiResponse<>();
        try {
            List<UserEntity> users = userRepository.findAll();
            List<UserResponseDto> userDtos = users.stream()
                    .map(this::mapToUserResponseDto)
                    .collect(Collectors.toList());

            response.setSuccess(true);
            response.setMessage("Users fetched successfully");
            response.setData(userDtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("An error occurred while fetching users: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        ApiResponse<UserResponseDto> response = new ApiResponse<>();
        try {
            UserEntity user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            response.setSuccess(true);
            response.setMessage("User fetched successfully");
            response.setData(mapToUserResponseDto(user));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("An error occurred while fetching the user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @RequestBody RegisterRequest updateRequest) {

        ApiResponse<UserResponseDto> response = new ApiResponse<>();

        try {
            UserEntity existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            existingUser.setFirstName(updateRequest.getFirstName());
            existingUser.setLastName(updateRequest.getLastName());
            existingUser.setAddress(updateRequest.getAddress());
            existingUser.setMobileNumber(updateRequest.getMobileNumber());
            existingUser.setAccountLocked(updateRequest.isActive());

            UserEntity updatedUser = userRepository.save(existingUser);

            response.setSuccess(true);
            response.setMessage("User updated successfully");
            response.setData(mapToUserResponseDto(updatedUser));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("An error occurred while updating the user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        ApiResponse<Void> response = new ApiResponse<>();
        try {
            if (!userRepository.existsById(id)) {
                response.setSuccess(false);
                response.setMessage("User not found");
                return ResponseEntity.status(404).body(response);
            }

            userRepository.deleteById(id);
            response.setSuccess(true);
            response.setMessage("User deleted successfully");
            response.setData(null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("An error occurred while deleting the user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
