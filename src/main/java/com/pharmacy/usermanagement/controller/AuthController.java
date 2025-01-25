package com.pharmacy.usermanagement.controller;

import com.pharmacy.usermanagement.dto.request.LoginRequest;
import com.pharmacy.usermanagement.dto.request.RegisterRequest;
import com.pharmacy.usermanagement.dto.response.ApiResponse;
import com.pharmacy.usermanagement.dto.response.LoginResponse;
import com.pharmacy.usermanagement.model.RoleEntity;
import com.pharmacy.usermanagement.model.UserEntity;
import com.pharmacy.usermanagement.repository.RoleRepository;
import com.pharmacy.usermanagement.repository.UserRepository;
import com.pharmacy.usermanagement.service.UserService;
import com.pharmacy.usermanagement.util.JwtTokenUtil;
import com.pharmacy.usermanagement.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenUtil jwtTokenUtil;

    private final UserRepository userRepository;

    private final UserService userService;

    private final RoleRepository roleRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = new LoginResponse();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            System.out.println("   ************************    login request  " + loginRequest.getEmail() + " " +
                    loginRequest.getPassword());
            String token = jwtTokenUtil.generateToken(authentication.getName());

            // Populate success response
            loginResponse.setSuccess(true);
            loginResponse.setMessage("Login successful");
            loginResponse.setToken(token);
            loginResponse.setRefreshToken(token);
            return ResponseEntity.ok(loginResponse);
        } catch (BadCredentialsException e) {
            // Populate failure response for invalid credentials
            loginResponse.setSuccess(false);
            loginResponse.setMessage("Invalid email or password");

            return ResponseEntity.status(401).body(loginResponse);
        } catch (Exception e) {
            // Populate failure response for other exceptions
            loginResponse.setSuccess(false);
            loginResponse.setMessage("An error occurred while processing your request");

            return ResponseEntity.status(500).body(loginResponse);
        }

    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserEntity>> registerUser(@RequestBody RegisterRequest registerRequest) {
        ApiResponse<UserEntity> response = new ApiResponse<>();

        try {
            // Check if the user already exists
            if (userService.existsByEmail(registerRequest.getEmail())) {
                response.setSuccess(false);
                response.setMessage("Email is already in use!");
                return ResponseEntity.badRequest().body(response); // HTTP 400
            }

            // Hash the password
            String hashedPassword = PasswordUtil.hashPassword(registerRequest.getPassword());

            // Create a new user entity and save it to the database
            UserEntity newUser = new UserEntity();
            newUser.setEmail(registerRequest.getEmail());
            newUser.setFirstName(registerRequest.getFirstName());
            newUser.setPassword(hashedPassword);
            newUser.setLastName(registerRequest.getLastName());
            newUser.setAddress(registerRequest.getAddress());
            newUser.setMobileNumber(registerRequest.getMobileNumber());

            // Save the user and set the response
            UserEntity savedUser = userRepository.save(newUser);

            response.setSuccess(true);
            response.setMessage("User registered successfully!");
            response.setData(savedUser);

            return ResponseEntity.ok(response); // HTTP 200
        } catch (Exception e) {
            // Handle unexpected errors
            response.setSuccess(false);
            response.setMessage("An error occurred while registering the user: " + e.getMessage());
            return ResponseEntity.status(500).body(response); // HTTP 500
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<UserEntity>> updateUser(
            @PathVariable Long id,
            @RequestBody RegisterRequest updateRequest) {

        ApiResponse<UserEntity> response = new ApiResponse<>();

        try {
            // Check if the user exists
            UserEntity existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update user details
            existingUser.setFirstName(updateRequest.getFirstName());
            existingUser.setLastName(updateRequest.getLastName());
            existingUser.setAddress(updateRequest.getAddress());
            existingUser.setMobileNumber(updateRequest.getMobileNumber());

            // Save updated user
            UserEntity updatedUser = userRepository.save(existingUser);
            updatedUser.setPassword("");

            // Success response
            response.setSuccess(true);
            response.setMessage("User updated successfully");
            response.setData(updatedUser);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Error response
            response.setSuccess(false);
            response.setMessage("An error occurred while updating the user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        ApiResponse<Void> response = new ApiResponse<>();

        try {
            // Check if the user exists
            if (!userRepository.existsById(id)) {
                response.setSuccess(false);
                response.setMessage("User not found");
                return ResponseEntity.status(404).body(response); // HTTP 404
            }

            // Delete the user
            userRepository.deleteById(id);

            // Success response
            response.setSuccess(true);
            response.setMessage("User deleted successfully");
            response.setData(null);

            return ResponseEntity.ok(response); // HTTP 200
        } catch (Exception e) {
            // Error response
            response.setSuccess(false);
            response.setMessage("An error occurred while deleting the user: " + e.getMessage());
            return ResponseEntity.status(500).body(response); // HTTP 500
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleEntity>>> getRoles() {
        ApiResponse<List<RoleEntity>> response = new ApiResponse<>();
        System.out.println("   ************************  getRoles ");
        try {
            // Fetch all roles
            List<RoleEntity> roles = roleRepository.findAll();

            // Success response
            response.setSuccess(true);
            response.setMessage("Roles fetched successfully");
            response.setData(roles);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Error response
            response.setSuccess(false);
            response.setMessage("An error occurred while fetching roles: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


}
