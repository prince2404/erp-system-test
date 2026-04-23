package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.request.geography.CreateDistrictRequest;
import com.apanaswastha.erp.dto.response.geography.DistrictResponse;

import java.util.List;

public interface DistrictService {

    /**
     * Creates a district.
     *
     * @param request create payload
     * @return created district
     */
    DistrictResponse create(CreateDistrictRequest request);

    /**
     * Lists all districts.
     *
     * @return district list
     */
    List<DistrictResponse> getAll();

    /**
     * Gets district by id.
     *
     * @param id district id
     * @return district details
     */
    DistrictResponse getById(Long id);
}
