package com.pharmacy.usermanagement.dto.request;

import lombok.Data;

@Data
public class UserRequestDto {

    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private String mobileNumber;
    private String password;
    private boolean isAccountLocked;

}
