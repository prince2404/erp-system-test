package com.apanaswastha.erp.service.impl;

import com.apanaswastha.erp.dto.request.geography.CreateStateRequest;
import com.apanaswastha.erp.dto.response.geography.StateResponse;
import com.apanaswastha.erp.entity.State;
import com.apanaswastha.erp.exception.NotFoundException;
import com.apanaswastha.erp.repository.StateRepository;
import lombok.extern.slf4j.Slf4j;
import com.apanaswastha.erp.service.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class StateServiceImpl implements StateService {

    private final StateRepository stateRepository;

    public StateServiceImpl(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    public StateResponse create(CreateStateRequest request) {
        State state = new State();
        state.setName(request.getName());
        state.setCode(request.getCode());
        return toResponse(stateRepository.save(state));
    }

    public List<StateResponse> getAll() {
        return stateRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public StateResponse getById(Long id) {
        State state = stateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("State not found with id: " + id));
        return toResponse(state);
    }

    private StateResponse toResponse(State state) {
        return new StateResponse(
                state.getId(),
                state.getName(),
                state.getCode(),
                state.getCreatedAt(),
                state.getUpdatedAt()
        );
    }
}
