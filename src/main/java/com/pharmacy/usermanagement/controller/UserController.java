package com.pharmacy.usermanagement.controller;

import com.pharmacy.usermanagement.dto.request.RegisterRequest;
import com.pharmacy.usermanagement.dto.request.UpdateUserRequest;
import com.pharmacy.usermanagement.dto.response.ApiResponse;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;


    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        ApiResponse<List<UserResponseDto>> response = new ApiResponse<>();

        List<UserEntity> users = userRepository.findAll();
        List<UserResponseDto> userDtos = users.stream()
                .map(userService::mapToUserResponseDto)
                .collect(Collectors.toList());

        response.setSuccess(true);
        response.setMessage("Users fetched successfully");
        response.setData(userDtos);
        return ResponseEntity.ok(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        ApiResponse<UserResponseDto> response = new ApiResponse<>();

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        response.setSuccess(true);
        response.setMessage("User fetched successfully");
        response.setData(userService.mapToUserResponseDto(user));
        return ResponseEntity.ok(response);

    }


    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        ApiResponse<UserResponseDto> response = new ApiResponse<>();

        userService.validateRegisterRequest(registerRequest);
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
                UserRoleEntity saveUserRoleEntity = userRoleRepository.save(userRole);
                savedUser.setUserRoles(List.of(saveUserRoleEntity));
            }
        }

//            UserEntity user = userRepository.findByIdWithRoles(savedUser.getUserId())
//                    .orElseThrow(() -> new RuntimeException("An error occurred while registering the user"));
//
        response.setSuccess(true);
        response.setMessage("User registered successfully!");
        response.setData(userService.mapToUserResponseDto(savedUser));

        return ResponseEntity.ok(response);

    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
          @Valid  @RequestBody UpdateUserRequest updateRequest) {

        ApiResponse<UserResponseDto> response = new ApiResponse<>();


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
        response.setData(userService.mapToUserResponseDto(updatedUser));

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        ApiResponse<Void> response = new ApiResponse<>();

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

    }
}
