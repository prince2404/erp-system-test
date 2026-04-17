package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.payload.ApiResponse;
import com.apanaswastha.erp.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ApiResponse<String> me(Authentication authentication) {
        return ApiResponse.success("Current user fetched", authentication.getName());
    }

    @GetMapping
    public ApiResponse<List<UserSummary>> listUsers() {
        List<UserSummary> users = userRepository.findAll().stream()
                .map(user -> new UserSummary(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole().getName().name()
                ))
                .toList();
        return ApiResponse.success("Users fetched", users);
    }

    public record UserSummary(Long id, String username, String email, String phone, String role) {
    }
}
