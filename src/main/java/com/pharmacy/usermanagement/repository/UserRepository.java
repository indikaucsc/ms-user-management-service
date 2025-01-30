package com.pharmacy.usermanagement.repository;




import com.pharmacy.usermanagement.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.userRoles WHERE u.id = :userId")
    Optional<UserEntity> findByIdWithRoles(@Param("userId") Long userId);


    boolean existsByEmail(String email);
}
