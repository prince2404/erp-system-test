package com.apanaswastha.erp.entity;

import com.apanaswastha.erp.enums.RoleName;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "permission_toggle_log")
public class PermissionToggleLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", nullable = false, length = 50)
    private RoleName targetRole;

    @Column(name = "permission", nullable = false, length = 10)
    private String permission;

    @Column(name = "old_value", nullable = false)
    private boolean oldValue;

    @Column(name = "new_value", nullable = false)
    private boolean newValue;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public Long getId() { return id; }
    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }
    public User getTargetUser() { return targetUser; }
    public void setTargetUser(User targetUser) { this.targetUser = targetUser; }
    public RoleName getTargetRole() { return targetRole; }
    public void setTargetRole(RoleName targetRole) { this.targetRole = targetRole; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public boolean isOldValue() { return oldValue; }
    public void setOldValue(boolean oldValue) { this.oldValue = oldValue; }
    public boolean isNewValue() { return newValue; }
    public void setNewValue(boolean newValue) { this.newValue = newValue; }
    public Instant getChangedAt() { return changedAt; }
}
