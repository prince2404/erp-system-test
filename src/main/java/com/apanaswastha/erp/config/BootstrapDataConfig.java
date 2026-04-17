package com.apanaswastha.erp.config;

import com.apanaswastha.erp.entity.Permission;
import com.apanaswastha.erp.entity.Role;
import com.apanaswastha.erp.entity.enums.RoleName;
import com.apanaswastha.erp.repository.PermissionRepository;
import com.apanaswastha.erp.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BootstrapDataConfig {

    @Bean
    CommandLineRunner seedRolesAndPermissions(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        return args -> {
            List<String> permissions = List.of(
                    "CREATE_USER",
                    "EDIT_USER",
                    "DELETE_USER",
                    "VIEW_REPORTS"
            );

            for (String permissionName : permissions) {
                permissionRepository.findByName(permissionName).orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(permissionName);
                    return permissionRepository.save(permission);
                });
            }

            for (RoleName roleName : RoleName.values()) {
                roleRepository.findByName(roleName).orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepository.save(role);
                });
            }
        };
    }
}
