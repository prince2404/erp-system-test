package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.request.auth.LoginRequest;
import com.apanaswastha.erp.dto.request.auth.RegisterRequest;
import com.apanaswastha.erp.dto.response.auth.AuthResponse;

public interface AuthService {

    /**
     * Registers a new user account and returns an authentication token.
     *
     * @param request registration payload
     * @return authentication response containing token
     * @throws IllegalArgumentException when user or referenced assignments are invalid
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates an existing user and returns an authentication token.
     *
     * @param request login payload
     * @return authentication response containing token
     * @throws org.springframework.security.core.AuthenticationException when credentials are invalid
     */
    AuthResponse login(LoginRequest request);
}
