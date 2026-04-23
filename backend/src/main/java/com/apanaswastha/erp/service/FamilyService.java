package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.request.family.CreateFamilyMemberRequest;
import com.apanaswastha.erp.dto.request.family.CreateFamilyRequest;
import com.apanaswastha.erp.dto.response.family.FamilyMemberResponse;
import com.apanaswastha.erp.dto.response.family.FamilyResponse;

import java.util.List;

public interface FamilyService {

    /**
     * Registers a family profile.
     *
     * @param request registration payload
     * @return created family
     */
    FamilyResponse registerFamily(CreateFamilyRequest request);

    /**
     * Adds a member to an existing family.
     *
     * @param healthCardNumber family identifier
     * @param request member payload
     * @return created family member
     */
    FamilyMemberResponse addFamilyMember(String healthCardNumber, CreateFamilyMemberRequest request);

    /**
     * Finds a family by health card number.
     *
     * @param healthCardNumber family identifier
     * @return family details
     */
    FamilyResponse getByHealthCardNumber(String healthCardNumber);

    /**
     * Lists all families.
     *
     * @return family list
     */
    List<FamilyResponse> getAllFamilies();
}
