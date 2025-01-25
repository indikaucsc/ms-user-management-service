package com.pharmacy.usermanagement.dto.response;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data; // This can be any type (e.g., user details, tokens, etc.)
}

