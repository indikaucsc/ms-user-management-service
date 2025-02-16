package com.pharmacy.usermanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {


    private String email;

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

}

