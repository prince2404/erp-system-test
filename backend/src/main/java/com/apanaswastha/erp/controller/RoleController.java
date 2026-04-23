package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.response.common.ApiResponse;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.service.CurrentUserService;
import com.apanaswastha.erp.service.PermissionGuardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/roles", "/api/v1/roles"})
public class RoleController {

    private final CurrentUserService currentUserService;
    private final PermissionGuardService permissionGuardService;

    public RoleController(CurrentUserService currentUserService, PermissionGuardService permissionGuardService) {
        this.currentUserService = currentUserService;
        this.permissionGuardService = permissionGuardService;
    }

    @GetMapping("/assignable")
    public ApiResponse<Map<String, List<String>>> getAssignableRoles() {
        User currentUser = currentUserService.requireCurrentUser();
        List<String> roles = permissionGuardService.assignableRoles(currentUser).stream()
                .map(role -> role.name().toLowerCase())
                .toList();
        return ApiResponse.success("Assignable roles fetched", Map.of("roles", roles));
    }
}
