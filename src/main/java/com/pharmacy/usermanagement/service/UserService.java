package com.pharmacy.usermanagement.service;

import com.pharmacy.usermanagement.dto.request.UserRequestDto;
import com.pharmacy.usermanagement.dto.response.UserResponseDto;
import com.pharmacy.usermanagement.model.UserEntity;
import com.pharmacy.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final int MAX_FAILED_ATTEMPTS = 3;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


   public boolean existsByEmail(String email){
       return userRepository.findByEmail(email).isPresent();
    }



    public void registerUser(UserRequestDto userDto) {
        UserEntity user = new UserEntity();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setAddress(userDto.getAddress());
        user.setAccountLocked(true);
        user.setMobileNumber(userDto.getMobileNumber());
        userRepository.save(user);
    }

    @Transactional
    public String loginUser(UserRequestDto userDto) {
        UserEntity user = userRepository.findByEmail(userDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isAccountLocked()) {
            throw new RuntimeException("Account is locked due to multiple failed login attempts.");
        }

        if (passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            resetFailedLoginAttempts(user);
            // Generate and return JWT token
            return "JWT-TOKEN"; // Replace with actual JWT token generation logic
        } else {
            incrementFailedLoginAttempts(user);
            throw new RuntimeException("Invalid credentials");
        }
    }

    private void incrementFailedLoginAttempts(UserEntity user) {
        int newAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newAttempts);
        if (newAttempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLocked(true);
        }
        userRepository.save(user);
    }

    private void resetFailedLoginAttempts(UserEntity user) {
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    public List<String> getRoles() {
        return List.of("ADMIN", "PHARMACIST", "STORE_MANAGER");
    }

    public void updateUser(Long id, UserRequestDto userDto) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setAddress(userDto.getAddress());
        user.setMobileNumber(userDto.getMobileNumber());
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void resetPassword(String email, String newPassword, String resetToken) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!resetToken.equals(user.getResetToken())) {
            throw new RuntimeException("Invalid reset token");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // Clear the reset token after password reset
        userRepository.save(user);
    }

    @Transactional
    public void generateResetToken(String email, String resetToken) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setResetToken(resetToken);
        userRepository.save(user);

        // Send reset token to the user's email or mobile number
        // Email/SMS sending logic goes here
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

}
