package com.pharmacy.usermanagement.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String address;
    private String mobileNumber;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int failedLoginAttempts;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isAccountLocked;

    private String resetToken;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters and Setters

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

