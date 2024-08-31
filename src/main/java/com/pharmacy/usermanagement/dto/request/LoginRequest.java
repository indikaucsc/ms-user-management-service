package com.pharmacy.usermanagement.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;

    // Getters and Setters
}
