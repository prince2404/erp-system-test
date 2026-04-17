package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.BlockResponse;
import com.apanaswastha.erp.dto.CreateBlockRequest;
import com.apanaswastha.erp.payload.ApiResponse;
import com.apanaswastha.erp.service.BlockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blocks")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BlockResponse>> create(@Valid @RequestBody CreateBlockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Block created successfully", blockService.create(request)));
    }

    @GetMapping
    public ApiResponse<List<BlockResponse>> getAll() {
        return ApiResponse.success("Blocks fetched successfully", blockService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<BlockResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Block fetched successfully", blockService.getById(id));
    }
}
