package com.apanaswastha.erp.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    private String email;

    private String phone;

    private Long roleId;

    private Long assignedStateId;

    private Long assignedDistrictId;

    private Long assignedBlockId;

    private Long assignedCenterId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getAssignedStateId() {
        return assignedStateId;
    }

    public void setAssignedStateId(Long assignedStateId) {
        this.assignedStateId = assignedStateId;
    }

    public Long getAssignedDistrictId() {
        return assignedDistrictId;
    }

    public void setAssignedDistrictId(Long assignedDistrictId) {
        this.assignedDistrictId = assignedDistrictId;
    }

    public Long getAssignedBlockId() {
        return assignedBlockId;
    }

    public void setAssignedBlockId(Long assignedBlockId) {
        this.assignedBlockId = assignedBlockId;
    }

    public Long getAssignedCenterId() {
        return assignedCenterId;
    }

    public void setAssignedCenterId(Long assignedCenterId) {
        this.assignedCenterId = assignedCenterId;
    }
}
