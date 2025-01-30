package com.pharmacy.usermanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 18 , message = "Password must be at least 8 characters long, no more 18 characters")
    private String password;

    @NotBlank(message = "First name is required.")
    @Size(max = 50, message = "First name should not exceed 50 characters")
    private String firstName;

    private String lastName;
    private String address;

    @NotBlank(message = "Mobile number is required.")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid mobile number format")
    private String mobileNumber;

    private boolean active;
    private List<Long> roleIds;
         /*   Admin 1
            Pharmacist 2
             Store Manager 3
             */

    // Getters and Setters
}

