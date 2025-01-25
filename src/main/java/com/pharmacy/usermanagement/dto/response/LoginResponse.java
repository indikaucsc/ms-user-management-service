package com.pharmacy.usermanagement.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private boolean success;
    private String message;
    private String token; // Only populated on successful login
    private String refreshToken;
}

