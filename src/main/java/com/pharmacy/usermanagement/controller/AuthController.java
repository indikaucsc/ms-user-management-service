package com.pharmacy.usermanagement.controller;

import com.pharmacy.usermanagement.dto.request.LoginRequest;
import com.pharmacy.usermanagement.dto.response.LoginResponse;
import com.pharmacy.usermanagement.service.UserService;
import com.pharmacy.usermanagement.util.JwtTokenUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = new LoginResponse();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));


        String token = jwtTokenUtil.generateToken(authentication.getName(), userService.getRoleIdByEmail(loginRequest.getEmail()));

        loginResponse.setSuccess(true);
        loginResponse.setMessage("Login successful");
        loginResponse.setToken(token);
        return ResponseEntity.ok(loginResponse);

    }


}
