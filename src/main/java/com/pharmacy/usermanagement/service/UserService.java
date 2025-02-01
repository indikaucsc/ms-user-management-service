package com.pharmacy.usermanagement.service;

import com.pharmacy.usermanagement.dto.request.RegisterRequest;
import com.pharmacy.usermanagement.dto.response.UserResponseDto;
import com.pharmacy.usermanagement.model.UserEntity;
import com.pharmacy.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final int MAX_FAILED_ATTEMPTS = 3;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String MOBILE_NUMBER_REGEX = "^\\+?[0-9]{10,15}$";

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }


    public Long getRoleIdByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User with email " + email + " not found"));

        return user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleId())
                .findFirst() // Retrieve the first role ID
                .orElseThrow(() -> new IllegalArgumentException("No roles found for user with email " + email));
    }

    public UserResponseDto mapToUserResponseDto(UserEntity user) {
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

    public void validateRegisterRequest(RegisterRequest request) {

        // Validate email
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!Pattern.matches(EMAIL_REGEX, request.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Validate password
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getPassword().length() < 8 || request.getPassword().length() > 18) {
            throw new IllegalArgumentException("Password must be between 8 and 18 characters long");
        }

        // Validate first name
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (request.getFirstName().length() > 50) {
            throw new IllegalArgumentException("First name should not exceed 50 characters");
        }

        // Validate mobile number
        if (request.getMobileNumber() == null || request.getMobileNumber().isBlank()) {
            throw new IllegalArgumentException("Mobile number is required.");
        }
        if (!Pattern.matches(MOBILE_NUMBER_REGEX, request.getMobileNumber())) {
            throw new IllegalArgumentException("Invalid mobile number format");
        }

        // Validate role IDs
        if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
            throw new IllegalArgumentException("At least one role ID is required");
        }
        List<Long> validRoleIds = List.of(1L, 2L, 3L); // Admin, Pharmacist, Store Manager
        for (Long roleId : request.getRoleIds()) {
            if (!validRoleIds.contains(roleId)) {
                throw new IllegalArgumentException("Role IDs must be 1 (Admin), 2 (Pharmacist), or 3 (Store Manager)");
            }
        }

    }

}
