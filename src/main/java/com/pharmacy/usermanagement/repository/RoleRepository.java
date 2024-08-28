package com.pharmacy.usermanagement.repository;


import com.pharmacy.usermanagement.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
}

