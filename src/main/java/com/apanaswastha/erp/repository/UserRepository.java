package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    Optional<User> findFirstByRoleNameAndIsDeletedFalseOrderByIdAsc(RoleName roleName);

    @Query("select count(u) > 0 from User u where u.isDeleted = false and (u.username = :username or u.email = :email)")
    boolean existsActiveByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
}
