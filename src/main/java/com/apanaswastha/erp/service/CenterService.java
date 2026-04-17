package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.CenterResponse;
import com.apanaswastha.erp.dto.CreateCenterRequest;
import com.apanaswastha.erp.entity.Block;
import com.apanaswastha.erp.entity.Center;
import com.apanaswastha.erp.repository.BlockRepository;
import com.apanaswastha.erp.repository.CenterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CenterService {

    private final CenterRepository centerRepository;
    private final BlockRepository blockRepository;

    public CenterService(CenterRepository centerRepository, BlockRepository blockRepository) {
        this.centerRepository = centerRepository;
        this.blockRepository = blockRepository;
    }

    public CenterResponse create(CreateCenterRequest request) {
        Block block = blockRepository.findById(request.getBlockId())
                .orElseThrow(() -> new IllegalArgumentException("Block not found with id: " + request.getBlockId()));

        Center center = new Center();
        center.setName(request.getName());
        center.setCenterCode(request.getCenterCode());
        center.setBlock(block);
        center.setAddress(request.getAddress());
        center.setContactNumber(request.getContactNumber());

        return toResponse(centerRepository.save(center));
    }

    public List<CenterResponse> getAll() {
        return centerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CenterResponse getById(Long id) {
        Center center = centerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Center not found with id: " + id));
        return toResponse(center);
    }

    private CenterResponse toResponse(Center center) {
        return new CenterResponse(
                center.getId(),
                center.getName(),
                center.getCenterCode(),
                center.getBlock().getId(),
                center.getAddress(),
                center.getContactNumber(),
                center.getCreatedAt(),
                center.getUpdatedAt()
        );
    }
}
