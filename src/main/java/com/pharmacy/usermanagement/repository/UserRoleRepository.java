package com.pharmacy.usermanagement.repository;


import com.pharmacy.usermanagement.model.RoleEntity;
import com.pharmacy.usermanagement.model.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {


}

