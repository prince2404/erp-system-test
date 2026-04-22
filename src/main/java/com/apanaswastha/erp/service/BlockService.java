package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.request.geography.CreateBlockRequest;
import com.apanaswastha.erp.dto.response.geography.BlockResponse;

import java.util.List;

public interface BlockService {

    /**
     * Creates a block.
     *
     * @param request create payload
     * @return created block
     */
    BlockResponse create(CreateBlockRequest request);

    /**
     * Returns all blocks.
     *
     * @return block list
     */
    List<BlockResponse> getAll();

    /**
     * Returns a block by id.
     *
     * @param id block id
     * @return block details
     */
    BlockResponse getById(Long id);
}
