package com.apanaswastha.erp.entity;

import com.apanaswastha.erp.enums.RoleName;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_permission_toggles", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "target_role"}))
public class UserPermissionToggle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", nullable = false, length = 50)
    private RoleName targetRole;

    @Column(name = "can_create", nullable = false)
    private boolean canCreate = true;

    @Column(name = "can_edit", nullable = false)
    private boolean canEdit = false;

    @Column(name = "can_delete", nullable = false)
    private boolean canDelete = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public RoleName getTargetRole() { return targetRole; }
    public void setTargetRole(RoleName targetRole) { this.targetRole = targetRole; }
    public boolean isCanCreate() { return canCreate; }
    public void setCanCreate(boolean canCreate) { this.canCreate = canCreate; }
    public boolean isCanEdit() { return canEdit; }
    public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }
    public boolean isCanDelete() { return canDelete; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }
    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}
