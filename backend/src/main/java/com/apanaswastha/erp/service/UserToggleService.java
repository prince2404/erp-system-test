package com.apanaswastha.erp.service;

import com.apanaswastha.erp.entity.PermissionToggleLog;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.entity.UserPermissionToggle;
import com.apanaswastha.erp.enums.RoleName;
import com.apanaswastha.erp.repository.PermissionToggleLogRepository;
import com.apanaswastha.erp.repository.UserPermissionToggleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserToggleService {

    private final UserPermissionToggleRepository toggleRepository;
    private final PermissionToggleLogRepository toggleLogRepository;

    public UserToggleService(UserPermissionToggleRepository toggleRepository,
                             PermissionToggleLogRepository toggleLogRepository) {
        this.toggleRepository = toggleRepository;
        this.toggleLogRepository = toggleLogRepository;
    }

    @Transactional
    public void initializeDefaultToggles(User user) {
        for (RoleName roleName : RoleName.values()) {
            if (roleName == RoleName.SUPER_ADMIN) {
                continue;
            }
            if (toggleRepository.findByUserIdAndTargetRole(user.getId(), roleName).isPresent()) {
                continue;
            }
            UserPermissionToggle toggle = new UserPermissionToggle();
            toggle.setUser(user);
            toggle.setTargetRole(roleName);
            toggle.setCanCreate(true);
            toggle.setCanEdit(false);
            toggle.setCanDelete(false);
            toggleRepository.save(toggle);
        }
    }

    public List<UserPermissionToggle> listForUser(Long userId) {
        return toggleRepository.findByUserId(userId);
    }

    @Transactional
    public Map<String, Object> updateToggle(User changedBy,
                                            User targetUser,
                                            RoleName targetRole,
                                            Boolean canCreate,
                                            Boolean canEdit,
                                            Boolean canDelete) {
        UserPermissionToggle toggle = toggleRepository.findByUserIdAndTargetRole(targetUser.getId(), targetRole)
                .orElseGet(() -> {
                    UserPermissionToggle created = new UserPermissionToggle();
                    created.setUser(targetUser);
                    created.setTargetRole(targetRole);
                    return created;
                });

        if (canCreate != null && toggle.isCanCreate() != canCreate) {
            saveLog(changedBy, targetUser, targetRole, "create", toggle.isCanCreate(), canCreate);
            toggle.setCanCreate(canCreate);
        }
        if (canEdit != null && toggle.isCanEdit() != canEdit) {
            saveLog(changedBy, targetUser, targetRole, "edit", toggle.isCanEdit(), canEdit);
            toggle.setCanEdit(canEdit);
        }
        if (canDelete != null && toggle.isCanDelete() != canDelete) {
            saveLog(changedBy, targetUser, targetRole, "delete", toggle.isCanDelete(), canDelete);
            toggle.setCanDelete(canDelete);
        }

        toggle.setUpdatedBy(changedBy);
        toggleRepository.save(toggle);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("targetRole", targetRole.name());
        response.put("canCreate", toggle.isCanCreate());
        response.put("canEdit", toggle.isCanEdit());
        response.put("canDelete", toggle.isCanDelete());
        return response;
    }

    private void saveLog(User changedBy, User targetUser, RoleName targetRole, String permission, boolean oldValue, boolean newValue) {
        PermissionToggleLog log = new PermissionToggleLog();
        log.setChangedBy(changedBy);
        log.setTargetUser(targetUser);
        log.setTargetRole(targetRole);
        log.setPermission(permission);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        toggleLogRepository.save(log);
    }
}
