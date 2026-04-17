package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    boolean existsByUsernameOrEmail(String username, String email);
}
