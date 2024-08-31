package com.pharmacy.usermanagement.controller;

import com.pharmacy.usermanagement.dto.request.LoginRequest;
import com.pharmacy.usermanagement.dto.request.RegisterRequest;
import com.pharmacy.usermanagement.model.UserEntity;
import com.pharmacy.usermanagement.repository.UserRepository;
import com.pharmacy.usermanagement.security.JwtTokenUtil;
import com.pharmacy.usermanagement.security.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public String authenticateUser(@RequestBody LoginRequest loginRequest) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        return jwtTokenUtil.generateToken(authentication.getName());
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody RegisterRequest registerRequest) {
        // Check if user already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return "Email is already in use!";
        }

        // Hash the password
        String hashedPassword = PasswordUtil.hashPassword(registerRequest.getPassword());

        // Create a new user entity and save it to the database
        UserEntity newUser = new UserEntity();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(hashedPassword);
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setAddress(registerRequest.getAddress());
        newUser.setMobileNumber(registerRequest.getMobileNumber());

        userRepository.save(newUser);

        return "User registered successfully!";
    }
}
