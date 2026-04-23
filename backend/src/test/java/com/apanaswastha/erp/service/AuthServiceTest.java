package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.response.auth.AuthResponse;
import com.apanaswastha.erp.dto.request.auth.LoginRequest;
import com.apanaswastha.erp.dto.request.auth.RegisterRequest;
import com.apanaswastha.erp.entity.Role;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.enums.RoleName;
import com.apanaswastha.erp.repository.BlockRepository;
import com.apanaswastha.erp.repository.CenterRepository;
import com.apanaswastha.erp.repository.DistrictRepository;
import com.apanaswastha.erp.repository.RoleRepository;
import com.apanaswastha.erp.repository.StateRepository;
import com.apanaswastha.erp.repository.UserRepository;
import com.apanaswastha.erp.security.JwtTokenProvider;
import com.apanaswastha.erp.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private StateRepository stateRepository;
    @Mock
    private DistrictRepository districtRepository;
    @Mock
    private BlockRepository blockRepository;
    @Mock
    private CenterRepository centerRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtService;
    @Mock
    private UserToggleService userToggleService;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private AuthSessionService authSessionService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerShouldEncodePasswordAssignDefaultRoleAndReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("plain-pass");
        request.setEmail("newuser@example.com");
        request.setPhone("1234567890");

        Role familyRole = new Role();
        familyRole.setName(RoleName.FAMILY);

        when(userRepository.existsActiveByUsernameOrEmail("newuser", "newuser@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.FAMILY)).thenReturn(Optional.of(familyRole));
        when(passwordEncoder.encode("plain-pass")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(UserDetails.class), anyString())).thenReturn("jwt-token");
        when(jwtService.extractExpiration("jwt-token")).thenReturn(new java.util.Date(System.currentTimeMillis() + 10000));

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encoded-pass", userCaptor.getValue().getPassword());
        assertEquals(RoleName.FAMILY, userCaptor.getValue().getRole().getName());
    }

    @Test
    void loginShouldAuthenticateAndReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("test-user");
        request.setPassword("password");

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        UserDetails principal = org.springframework.security.core.userdetails.User.builder()
                .username("test-user")
                .password("password")
                .authorities("ROLE_FAMILY")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtService.generateToken(eq(principal), anyString())).thenReturn("jwt-login");
        when(jwtService.extractExpiration("jwt-login")).thenReturn(new java.util.Date(System.currentTimeMillis() + 10000));
        User foundUser = new User();
        foundUser.setUsername("test-user");
        when(userRepository.findByUsernameAndIsDeletedFalse("test-user")).thenReturn(Optional.of(foundUser));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-login", response.getToken());
    }
}
