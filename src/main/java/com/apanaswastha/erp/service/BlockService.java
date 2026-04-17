package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.BlockResponse;
import com.apanaswastha.erp.dto.CreateBlockRequest;
import com.apanaswastha.erp.entity.Block;
import com.apanaswastha.erp.entity.District;
import com.apanaswastha.erp.repository.BlockRepository;
import com.apanaswastha.erp.repository.DistrictRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockService {

    private final BlockRepository blockRepository;
    private final DistrictRepository districtRepository;

    public BlockService(BlockRepository blockRepository, DistrictRepository districtRepository) {
        this.blockRepository = blockRepository;
        this.districtRepository = districtRepository;
    }

    public BlockResponse create(CreateBlockRequest request) {
        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new IllegalArgumentException("District not found with id: " + request.getDistrictId()));

        Block block = new Block();
        block.setName(request.getName());
        block.setDistrict(district);

        return toResponse(blockRepository.save(block));
    }

    public List<BlockResponse> getAll() {
        return blockRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public BlockResponse getById(Long id) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Block not found with id: " + id));
        return toResponse(block);
    }

    private BlockResponse toResponse(Block block) {
        return new BlockResponse(
                block.getId(),
                block.getName(),
                block.getDistrict().getId(),
                block.getCreatedAt(),
                block.getUpdatedAt()
        );
    }
}
