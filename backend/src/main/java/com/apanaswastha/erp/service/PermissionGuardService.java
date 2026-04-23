package com.apanaswastha.erp.service;

import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.entity.UserPermissionToggle;
import com.apanaswastha.erp.enums.RoleName;
import com.apanaswastha.erp.repository.UserPermissionToggleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PermissionGuardService {

    public enum Action {
        CREATE,
        EDIT,
        DELETE
    }

    private static final Map<RoleName, Set<RoleName>> CREATE_MAP = new EnumMap<>(RoleName.class);
    private static final Set<RoleName> CENTER_LEVEL_ROLES = EnumSet.of(
            RoleName.DOCTOR,
            RoleName.PHARMACIST,
            RoleName.RECEPTIONIST,
            RoleName.HR_MANAGER,
            RoleName.STAFF,
            RoleName.ASSOCIATE
    );

    static {
        CREATE_MAP.put(RoleName.SUPER_ADMIN, EnumSet.allOf(RoleName.class));
        CREATE_MAP.put(RoleName.ADMIN, EnumSet.of(
                RoleName.STATE_MANAGER,
                RoleName.DISTRICT_MANAGER,
                RoleName.BLOCK_MANAGER,
                RoleName.DOCTOR,
                RoleName.PHARMACIST,
                RoleName.RECEPTIONIST,
                RoleName.HR_MANAGER,
                RoleName.STAFF,
                RoleName.ASSOCIATE,
                RoleName.FAMILY
        ));
        CREATE_MAP.put(RoleName.STATE_MANAGER, EnumSet.of(RoleName.DISTRICT_MANAGER));
        CREATE_MAP.put(RoleName.DISTRICT_MANAGER, EnumSet.of(RoleName.BLOCK_MANAGER));
        CREATE_MAP.put(RoleName.BLOCK_MANAGER, CENTER_LEVEL_ROLES);
    }

    private final UserPermissionToggleRepository toggleRepository;

    public PermissionGuardService(UserPermissionToggleRepository toggleRepository) {
        this.toggleRepository = toggleRepository;
    }

    public List<RoleName> assignableRoles(User actor) {
        Set<RoleName> roles = CREATE_MAP.getOrDefault(actor.getRole().getName(), EnumSet.noneOf(RoleName.class));
        return roles.stream().filter(this::isAssignableRole).toList();
    }

    public void assertCanManage(User actor, User target, Action action) {
        RoleName actorRole = actor.getRole().getName();
        RoleName targetRole = target.getRole().getName();
        if (!isHierarchyAllowed(actorRole, targetRole, action)) {
            throw new AccessDeniedException("Role hierarchy violation");
        }
        if (!isWithinScope(actor, target)) {
            throw new AccessDeniedException("Geographic scope violation");
        }
        if (requiresToggle(actorRole) && !isToggleAllowed(actor, targetRole, action)) {
            throw new AccessDeniedException("Permission toggle is OFF");
        }
    }

    public void assertCanAssignRole(User actor, RoleName targetRole) {
        RoleName actorRole = actor.getRole().getName();
        if (!CREATE_MAP.getOrDefault(actorRole, EnumSet.noneOf(RoleName.class)).contains(targetRole)) {
            throw new AccessDeniedException("Cannot assign this role");
        }
        if (requiresToggle(actorRole) && !isToggleAllowed(actor, targetRole, Action.CREATE)) {
            throw new AccessDeniedException("CREATE toggle is OFF");
        }
    }

    public boolean isWithinScope(User actor, User target) {
        RoleName role = actor.getRole().getName();
        if (role == RoleName.SUPER_ADMIN || role == RoleName.ADMIN) {
            return true;
        }
        if (role == RoleName.STATE_MANAGER) {
            return actor.getAssignedState() != null && target.getAssignedState() != null
                    && actor.getAssignedState().getId().equals(target.getAssignedState().getId());
        }
        if (role == RoleName.DISTRICT_MANAGER) {
            return actor.getAssignedDistrict() != null && target.getAssignedDistrict() != null
                    && actor.getAssignedDistrict().getId().equals(target.getAssignedDistrict().getId());
        }
        if (role == RoleName.BLOCK_MANAGER) {
            return actor.getAssignedBlock() != null && target.getAssignedBlock() != null
                    && actor.getAssignedBlock().getId().equals(target.getAssignedBlock().getId());
        }
        if (CENTER_LEVEL_ROLES.contains(role)) {
            return actor.getAssignedCenter() != null && target.getAssignedCenter() != null
                    && actor.getAssignedCenter().getId().equals(target.getAssignedCenter().getId());
        }
        return actor.getId().equals(target.getId());
    }

    private boolean isHierarchyAllowed(RoleName actorRole, RoleName targetRole, Action action) {
        if (actorRole == RoleName.SUPER_ADMIN) {
            return true;
        }
        if (action == Action.DELETE && actorRole == RoleName.ADMIN && targetRole == RoleName.SUPER_ADMIN) {
            return false;
        }
        return CREATE_MAP.getOrDefault(actorRole, EnumSet.noneOf(RoleName.class)).contains(targetRole);
    }

    private boolean requiresToggle(RoleName actorRole) {
        return actorRole == RoleName.STATE_MANAGER
                || actorRole == RoleName.DISTRICT_MANAGER
                || actorRole == RoleName.BLOCK_MANAGER;
    }

    private boolean isToggleAllowed(User actor, RoleName targetRole, Action action) {
        UserPermissionToggle toggle = toggleRepository.findByUserIdAndTargetRole(actor.getId(), targetRole)
                .orElse(null);
        if (toggle == null) {
            return action == Action.CREATE;
        }
        return switch (action) {
            case CREATE -> toggle.isCanCreate();
            case EDIT -> toggle.isCanEdit();
            case DELETE -> toggle.isCanDelete();
        };
    }

    private boolean isAssignableRole(RoleName roleName) {
        return roleName != RoleName.SUPER_ADMIN;
    }
}
