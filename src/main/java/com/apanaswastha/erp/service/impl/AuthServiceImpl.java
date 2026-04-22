package com.apanaswastha.erp.service.impl;

import com.apanaswastha.erp.dto.response.auth.AuthResponse;
import com.apanaswastha.erp.dto.request.auth.LoginRequest;
import com.apanaswastha.erp.dto.request.auth.RegisterRequest;
import com.apanaswastha.erp.entity.Block;
import com.apanaswastha.erp.entity.Center;
import com.apanaswastha.erp.entity.District;
import com.apanaswastha.erp.entity.Role;
import com.apanaswastha.erp.entity.State;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.enums.RoleName;
import com.apanaswastha.erp.repository.BlockRepository;
import com.apanaswastha.erp.repository.CenterRepository;
import com.apanaswastha.erp.repository.DistrictRepository;
import com.apanaswastha.erp.repository.RoleRepository;
import com.apanaswastha.erp.repository.StateRepository;
import com.apanaswastha.erp.repository.UserRepository;
import com.apanaswastha.erp.security.JwtTokenProvider;
import com.apanaswastha.erp.service.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final BlockRepository blockRepository;
    private final CenterRepository centerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            StateRepository stateRepository,
            DistrictRepository districtRepository,
            BlockRepository blockRepository,
            CenterRepository centerRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.stateRepository = stateRepository;
        this.districtRepository = districtRepository;
        this.blockRepository = blockRepository;
        this.centerRepository = centerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsActiveByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            throw new IllegalArgumentException("Username or email already exists");
        }

        Role role = resolveRole(request.getRoleId());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setAssignedState(resolveState(request.getAssignedStateId()));
        user.setAssignedDistrict(resolveDistrict(request.getAssignedDistrictId()));
        user.setAssignedBlock(resolveBlock(request.getAssignedBlockId()));
        user.setAssignedCenter(resolveCenter(request.getAssignedCenterId()));
        User savedUser = userRepository.save(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(savedUser.getUsername())
                .password(savedUser.getPassword())
                .authorities("ROLE_" + savedUser.getRole().getName().name())
                .build();

        return new AuthResponse(jwtService.generateToken(userDetails));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return new AuthResponse(jwtService.generateToken(principal));
    }

    private Role resolveRole(Long roleId) {
        if (roleId != null) {
            return roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid role id"));
        }
        return roleRepository.findByName(RoleName.FAMILY)
                .orElseThrow(() -> new IllegalStateException("Default role FAMILY is not configured"));
    }

    private State resolveState(Long stateId) {
        if (stateId == null) {
            return null;
        }
        return stateRepository.findById(stateId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid assigned state id"));
    }

    private District resolveDistrict(Long districtId) {
        if (districtId == null) {
            return null;
        }
        return districtRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid assigned district id"));
    }

    private Block resolveBlock(Long blockId) {
        if (blockId == null) {
            return null;
        }
        return blockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid assigned block id"));
    }

    private Center resolveCenter(Long centerId) {
        if (centerId == null) {
            return null;
        }
        return centerRepository.findById(centerId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid assigned center id"));
    }
}
