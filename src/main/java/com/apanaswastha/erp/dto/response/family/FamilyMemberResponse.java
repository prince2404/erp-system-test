package com.apanaswastha.erp.dto.response.family;

import java.time.Instant;
import java.time.LocalDate;

public class FamilyMemberResponse {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final LocalDate dob;
    private final String gender;
    private final String bloodGroup;
    private final Long familyId;
    private final Instant createdAt;
    private final Instant updatedAt;

    public FamilyMemberResponse(
            Long id,
            String firstName,
            String lastName,
            LocalDate dob,
            String gender,
            String bloodGroup,
            Long familyId,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.familyId = familyId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public Long getFamilyId() {
        return familyId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
