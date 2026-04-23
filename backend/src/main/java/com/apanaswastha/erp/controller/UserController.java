package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.response.common.ApiResponse;
import com.apanaswastha.erp.entity.Role;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.entity.UserPermissionToggle;
import com.apanaswastha.erp.enums.RoleName;
import com.apanaswastha.erp.enums.UserStatus;
import com.apanaswastha.erp.repository.*;
import com.apanaswastha.erp.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping({"/api/users", "/api/v1/users"})
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final BlockRepository blockRepository;
    private final CenterRepository centerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final PermissionGuardService permissionGuardService;
    private final UserToggleService userToggleService;
    private final UserProfileService userProfileService;
    private final AuthSessionService authSessionService;

    public UserController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          StateRepository stateRepository,
                          DistrictRepository districtRepository,
                          BlockRepository blockRepository,
                          CenterRepository centerRepository,
                          PasswordEncoder passwordEncoder,
                          CurrentUserService currentUserService,
                          PermissionGuardService permissionGuardService,
                          UserToggleService userToggleService,
                          UserProfileService userProfileService,
                          AuthSessionService authSessionService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.stateRepository = stateRepository;
        this.districtRepository = districtRepository;
        this.blockRepository = blockRepository;
        this.centerRepository = centerRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
        this.permissionGuardService = permissionGuardService;
        this.userToggleService = userToggleService;
        this.userProfileService = userProfileService;
        this.authSessionService = authSessionService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfile> me() {
        User user = currentUserService.requireCurrentUser();
        UserProfile profile = new UserProfile(
                user.getId(),
                user.getUsername(),
                user.getRole().getName().name(),
                user.getAssignedCenter() != null ? user.getAssignedCenter().getId() : null,
                user.getStatus().name()
        );
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
                        user.getRole().getName().name(),
                        user.getStatus().name()
                ));
        return ApiResponse.success("Users fetched", users);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserSummary>> createManagedUser(@RequestBody CreateManagedUserRequest request) {
        User actor = currentUserService.requireCurrentUser();
        if (request.role() == null) {
            throw new IllegalArgumentException("role is required");
        }
        permissionGuardService.assertCanAssignRole(actor, request.role());

        if (userRepository.existsActiveByUsernameOrEmail(request.username(), request.email())) {
            throw new IllegalArgumentException("Username or email already exists");
        }

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role"));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);

        user.setAssignedState(resolveState(actor, request.assignedStateId()));
        user.setAssignedDistrict(resolveDistrict(actor, request.assignedDistrictId()));
        user.setAssignedBlock(resolveBlock(actor, request.assignedBlockId()));
        user.setAssignedCenter(resolveCenter(actor, request.assignedCenterId()));
        user.setStatus(UserStatus.ACTIVE);

        User saved = userRepository.save(user);
        userToggleService.initializeDefaultToggles(saved);
        userProfileService.initializeProfileTables(saved);

        UserSummary response = new UserSummary(
                saved.getId(), saved.getUsername(), saved.getEmail(), saved.getPhone(), saved.getRole().getName().name(), saved.getStatus().name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User created", response));
    }

    @PatchMapping("/{id}/safe")
    public ApiResponse<Map<String, Object>> safeEdit(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        User actor = currentUserService.requireCurrentUser();
        User target = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        permissionGuardService.assertCanManage(actor, target, PermissionGuardService.Action.EDIT);
        rejectDangerous(payload);

        if (payload.containsKey("username")) target.setUsername(String.valueOf(payload.get("username")));
        if (payload.containsKey("email")) target.setEmail(String.valueOf(payload.get("email")));
        if (payload.containsKey("phone")) target.setPhone(String.valueOf(payload.get("phone")));
        userRepository.save(target);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", target.getId());
        result.put("username", target.getUsername());
        result.put("email", target.getEmail());
        result.put("phone", target.getPhone());
        return ApiResponse.success("User safely updated", result);
    }

    @PostMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        User actor = currentUserService.requireCurrentUser();
        User target = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        permissionGuardService.assertCanManage(actor, target, PermissionGuardService.Action.DELETE);
        target.setStatus(UserStatus.DEACTIVATED);
        userRepository.save(target);
        authSessionService.revokeAllSessions(target.getId());
        return ApiResponse.success("User deactivated", null);
    }

    @PostMapping("/{id}/reactivate")
    public ApiResponse<Void> reactivate(@PathVariable Long id) {
        User actor = currentUserService.requireCurrentUser();
        User target = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        permissionGuardService.assertCanManage(actor, target, PermissionGuardService.Action.DELETE);
        target.setStatus(UserStatus.ACTIVE);
        userRepository.save(target);
        return ApiResponse.success("User reactivated", null);
    }

    @GetMapping("/{id}/editable")
    public ApiResponse<Map<String, Object>> getEditableForm(@PathVariable Long id) {
        User actor = currentUserService.requireCurrentUser();
        User target = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        permissionGuardService.assertCanManage(actor, target, PermissionGuardService.Action.EDIT);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", target.getId());
        response.put("username", target.getUsername());
        response.put("email", target.getEmail());
        response.put("phone", target.getPhone());

        if (actor.getRole().getName() == RoleName.ADMIN || actor.getRole().getName() == RoleName.SUPER_ADMIN) {
            response.put("role", target.getRole().getName().name());
            response.put("assignedStateId", target.getAssignedState() != null ? target.getAssignedState().getId() : null);
            response.put("assignedDistrictId", target.getAssignedDistrict() != null ? target.getAssignedDistrict().getId() : null);
            response.put("assignedBlockId", target.getAssignedBlock() != null ? target.getAssignedBlock().getId() : null);
            response.put("assignedCenterId", target.getAssignedCenter() != null ? target.getAssignedCenter().getId() : null);
        }
        return ApiResponse.success("Editable fields fetched", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> hardDeleteBlocked(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("Hard delete is not allowed. Use deactivation only."));
    }

    @GetMapping("/{id}/profile")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> getUserProfile(@PathVariable Long id) {
        return ApiResponse.success("User profile fetched", userProfileService.getProfileByUserId(id));
    }

    @PatchMapping("/{id}/personal")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Map<String, Object>> patchUserPersonal(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success("User personal info updated", userProfileService.updatePersonalByAdmin(currentUserService.requireCurrentUser(), id, payload));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Map<String, Object>> patchUserWithDangerousFields(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        User target = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (payload.containsKey("role")) {
            RoleName roleName = RoleName.valueOf(String.valueOf(payload.get("role")));
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid role"));
            target.setRole(role);
        }
        if (payload.containsKey("assignedStateId")) {
            Long stateId = payload.get("assignedStateId") == null ? null : Long.valueOf(String.valueOf(payload.get("assignedStateId")));
            target.setAssignedState(stateId == null ? null : stateRepository.findById(stateId).orElseThrow(() -> new IllegalArgumentException("Invalid state id")));
        }
        if (payload.containsKey("assignedDistrictId")) {
            Long districtId = payload.get("assignedDistrictId") == null ? null : Long.valueOf(String.valueOf(payload.get("assignedDistrictId")));
            target.setAssignedDistrict(districtId == null ? null : districtRepository.findById(districtId).orElseThrow(() -> new IllegalArgumentException("Invalid district id")));
        }
        if (payload.containsKey("assignedBlockId")) {
            Long blockId = payload.get("assignedBlockId") == null ? null : Long.valueOf(String.valueOf(payload.get("assignedBlockId")));
            target.setAssignedBlock(blockId == null ? null : blockRepository.findById(blockId).orElseThrow(() -> new IllegalArgumentException("Invalid block id")));
        }
        if (payload.containsKey("assignedCenterId")) {
            Long centerId = payload.get("assignedCenterId") == null ? null : Long.valueOf(String.valueOf(payload.get("assignedCenterId")));
            target.setAssignedCenter(centerId == null ? null : centerRepository.findById(centerId).orElseThrow(() -> new IllegalArgumentException("Invalid center id")));
        }
        userRepository.save(target);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", target.getId());
        response.put("role", target.getRole().getName().name());
        response.put("assignedStateId", target.getAssignedState() != null ? target.getAssignedState().getId() : null);
        response.put("assignedDistrictId", target.getAssignedDistrict() != null ? target.getAssignedDistrict().getId() : null);
        response.put("assignedBlockId", target.getAssignedBlock() != null ? target.getAssignedBlock().getId() : null);
        response.put("assignedCenterId", target.getAssignedCenter() != null ? target.getAssignedCenter().getId() : null);
        return ApiResponse.success("User updated with dangerous fields", response);
    }

    @GetMapping("/{id}/toggles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getToggles(@PathVariable Long id) {
        List<Map<String, Object>> data = userToggleService.listForUser(id).stream().map(this::toToggleMap).toList();
        return ApiResponse.success("User toggles fetched", data);
    }

    @PatchMapping("/{id}/toggles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> patchToggles(@PathVariable Long id, @RequestBody ToggleUpdateRequest request) {
        User actor = currentUserService.requireCurrentUser();
        User target = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ApiResponse.success(
                "User toggles updated",
                userToggleService.updateToggle(actor, target, request.targetRole(), request.canCreate(), request.canEdit(), request.canDelete())
        );
    }

    @PostMapping("/{id}/bank-accounts/{bid}/verify")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Void> verifyBank(@PathVariable Long id, @PathVariable Long bid) {
        userProfileService.verifyBankAccount(currentUserService.requireCurrentUser(), id, bid);
        return ApiResponse.success("Bank account verified", null);
    }

    @PostMapping("/{id}/verify/aadhaar/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Map<String, Object>> reviewAadhaar(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return ApiResponse.success("Aadhaar reviewed", userProfileService.reviewAadhaar(currentUserService.requireCurrentUser(), id, request.approve(), request.reason()));
    }

    @PostMapping("/{id}/verify/photo-id/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Map<String, Object>> reviewPhotoId(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return ApiResponse.success("Photo ID reviewed", userProfileService.reviewPhotoId(currentUserService.requireCurrentUser(), id, request.approve(), request.reason()));
    }

    private void rejectDangerous(Map<String, Object> payload) {
        Set<String> dangerous = Set.of("role", "roleId", "assignedStateId", "assignedDistrictId", "assignedBlockId", "assignedCenterId", "commission", "commissionRate", "scope");
        for (String key : payload.keySet()) {
            if (dangerous.contains(key)) {
                throw new AccessDeniedException("Dangerous fields are not allowed");
            }
        }
    }

    private com.apanaswastha.erp.entity.State resolveState(User actor, Long stateId) {
        if (stateId == null) {
            return actor.getRole().getName() == RoleName.STATE_MANAGER ? actor.getAssignedState() : null;
        }
        com.apanaswastha.erp.entity.State state = stateRepository.findById(stateId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid assigned state id"));
        if (actor.getRole().getName() == RoleName.STATE_MANAGER
                && (actor.getAssignedState() == null || !actor.getAssignedState().getId().equals(stateId))) {
            throw new AccessDeniedException("State scope violation");
        }
        return state;
    }

    private com.apanaswastha.erp.entity.District resolveDistrict(User actor, Long districtId) {
        if (districtId == null) {
            return actor.getRole().getName() == RoleName.DISTRICT_MANAGER ? actor.getAssignedDistrict() : null;
        }
        com.apanaswastha.erp.entity.District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid assigned district id"));
        if (actor.getRole().getName() == RoleName.DISTRICT_MANAGER
                && (actor.getAssignedDistrict() == null || !actor.getAssignedDistrict().getId().equals(districtId))) {
            throw new AccessDeniedException("District scope violation");
        }
        if (actor.getRole().getName() == RoleName.STATE_MANAGER
                && (actor.getAssignedState() == null || !district.getState().getId().equals(actor.getAssignedState().getId()))) {
            throw new AccessDeniedException("District outside your state scope");
        }
        return district;
    }

    private com.apanaswastha.erp.entity.Block resolveBlock(User actor, Long blockId) {
        if (blockId == null) {
            return actor.getRole().getName() == RoleName.BLOCK_MANAGER ? actor.getAssignedBlock() : null;
        }
        com.apanaswastha.erp.entity.Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid assigned block id"));
        if (actor.getRole().getName() == RoleName.BLOCK_MANAGER
                && (actor.getAssignedBlock() == null || !actor.getAssignedBlock().getId().equals(blockId))) {
            throw new AccessDeniedException("Block scope violation");
        }
        if (actor.getRole().getName() == RoleName.DISTRICT_MANAGER
                && (actor.getAssignedDistrict() == null || !block.getDistrict().getId().equals(actor.getAssignedDistrict().getId()))) {
            throw new AccessDeniedException("Block outside your district scope");
        }
        return block;
    }

    private com.apanaswastha.erp.entity.Center resolveCenter(User actor, Long centerId) {
        if (centerId == null) {
            return actor.getAssignedCenter();
        }
        com.apanaswastha.erp.entity.Center center = centerRepository.findById(centerId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid assigned center id"));
        if (actor.getRole().getName() == RoleName.BLOCK_MANAGER
                && (actor.getAssignedBlock() == null || !center.getBlock().getId().equals(actor.getAssignedBlock().getId()))) {
            throw new AccessDeniedException("Center outside your block scope");
        }
        return center;
    }

    private Map<String, Object> toToggleMap(UserPermissionToggle toggle) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("targetRole", toggle.getTargetRole().name().toLowerCase());
        map.put("canCreate", toggle.isCanCreate());
        map.put("canEdit", toggle.isCanEdit());
        map.put("canDelete", toggle.isCanDelete());
        map.put("updatedAt", toggle.getUpdatedAt());
        map.put("updatedBy", toggle.getUpdatedBy() != null ? toggle.getUpdatedBy().getId() : null);
        return map;
    }

    public record UserSummary(Long id, String username, String email, String phone, String role, String status) {
    }

    public record UserProfile(Long id, String username, String role, Long assignedCenterId, String status) {
    }

    public record CreateManagedUserRequest(String username,
                                           String password,
                                           String email,
                                           String phone,
                                           RoleName role,
                                           Long assignedStateId,
                                           Long assignedDistrictId,
                                           Long assignedBlockId,
                                           Long assignedCenterId) {
    }

    public record ToggleUpdateRequest(RoleName targetRole, Boolean canCreate, Boolean canEdit, Boolean canDelete) {
    }

    public record ReviewRequest(boolean approve, String reason) {
    }
}
