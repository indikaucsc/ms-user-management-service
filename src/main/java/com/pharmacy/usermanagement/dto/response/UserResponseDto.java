package com.pharmacy.usermanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String address;
    private String mobileNumber;
    private boolean active;
    private List<Long> roleIds;



         /*   Admin 1
            Pharmacist 2
             Store Manager 3
             */

    // Getters and Setters
}

