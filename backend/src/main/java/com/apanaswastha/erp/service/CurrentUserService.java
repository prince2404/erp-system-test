package com.apanaswastha.erp.service;

import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.enums.UserStatus;
import com.apanaswastha.erp.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Unauthenticated user");
        }
        return userRepository.findByUsernameAndIsDeletedFalseAndStatus(authentication.getName(), UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
    }
}
