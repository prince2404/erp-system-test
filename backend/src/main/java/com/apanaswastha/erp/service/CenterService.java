package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.request.geography.CreateCenterRequest;
import com.apanaswastha.erp.dto.response.geography.CenterResponse;

import java.util.List;

public interface CenterService {

    /**
     * Creates a center.
     *
     * @param request create payload
     * @return created center
     */
    CenterResponse create(CreateCenterRequest request);

    /**
     * Lists all centers.
     *
     * @return center list
     */
    List<CenterResponse> getAll();

    /**
     * Gets center by id.
     *
     * @param id center id
     * @return center details
     */
    CenterResponse getById(Long id);
}
