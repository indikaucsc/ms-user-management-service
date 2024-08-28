package com.pharmacy.usermanagement.model;



import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "`role`")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Getters and Setters
}




