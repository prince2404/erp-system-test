package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.AuthResponse;
import com.apanaswastha.erp.dto.LoginRequest;
import com.apanaswastha.erp.dto.RegisterRequest;
import com.apanaswastha.erp.entity.Role;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.entity.enums.RoleName;
import com.apanaswastha.erp.repository.RoleRepository;
import com.apanaswastha.erp.repository.UserRepository;
import com.apanaswastha.erp.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
}
