package com.apanaswastha.erp.service.impl;

import com.apanaswastha.erp.dto.request.geography.CreateDistrictRequest;
import com.apanaswastha.erp.dto.response.geography.DistrictResponse;
import com.apanaswastha.erp.entity.District;
import com.apanaswastha.erp.entity.State;
import com.apanaswastha.erp.exception.NotFoundException;
import com.apanaswastha.erp.repository.DistrictRepository;
import com.apanaswastha.erp.repository.StateRepository;
import lombok.extern.slf4j.Slf4j;
import com.apanaswastha.erp.service.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;
    private final StateRepository stateRepository;

    public DistrictServiceImpl(DistrictRepository districtRepository, StateRepository stateRepository) {
        this.districtRepository = districtRepository;
        this.stateRepository = stateRepository;
    }

    public DistrictResponse create(CreateDistrictRequest request) {
        State state = stateRepository.findById(request.getStateId())
                .orElseThrow(() -> new NotFoundException("State not found with id: " + request.getStateId()));

        District district = new District();
        district.setName(request.getName());
        district.setState(state);

        return toResponse(districtRepository.save(district));
    }

    public List<DistrictResponse> getAll() {
        return districtRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public DistrictResponse getById(Long id) {
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("District not found with id: " + id));
        return toResponse(district);
    }

    private DistrictResponse toResponse(District district) {
        return new DistrictResponse(
                district.getId(),
                district.getName(),
                district.getState().getId(),
                district.getCreatedAt(),
                district.getUpdatedAt()
        );
    }
}
