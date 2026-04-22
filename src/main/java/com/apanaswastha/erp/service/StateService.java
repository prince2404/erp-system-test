package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.request.geography.CreateStateRequest;
import com.apanaswastha.erp.dto.response.geography.StateResponse;

import java.util.List;

public interface StateService {

    /**
     * Creates a state.
     *
     * @param request create payload
     * @return created state
     */
    StateResponse create(CreateStateRequest request);

    /**
     * Lists all states.
     *
     * @return state list
     */
    List<StateResponse> getAll();

    /**
     * Gets state by id.
     *
     * @param id state id
     * @return state details
     */
    StateResponse getById(Long id);
}
