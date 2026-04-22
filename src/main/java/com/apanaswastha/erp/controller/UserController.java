package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.response.common.ApiResponse;
import com.apanaswastha.erp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfile> me(Authentication authentication) {
        UserProfile profile = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .map(user -> new UserProfile(
                        user.getId(),
                        user.getUsername(),
                        user.getRole().getName().name(),
                        user.getAssignedCenter() != null ? user.getAssignedCenter().getId() : null
                ))
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        return ApiResponse.success("Current user fetched", profile);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','HR_MANAGER')")
    public ApiResponse<Page<UserSummary>> listUsers(Pageable pageable) {
        Page<UserSummary> users = userRepository.findAll(pageable)
                .map(user -> new UserSummary(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole().getName().name()
                ));
        return ApiResponse.success("Users fetched", users);
    }

    public record UserSummary(Long id, String username, String email, String phone, String role) {
    }

    public record UserProfile(Long id, String username, String role, Long assignedCenterId) {
    }
}
