package com.apanaswastha.erp.security;

import com.apanaswastha.erp.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String SECRET = "ZmFrZS1kZW1vLXNlY3JldC1rZXktdGhhdC1tdXN0LWJlLTMyLWJ5dGVzLWxvbmc=";

    private JwtTokenProvider jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setExpirationMs(60_000);

        jwtService = new JwtTokenProvider(jwtProperties);
        userDetails = User.builder()
                .username("test-user")
                .password("password")
                .authorities("ROLE_FAMILY")
                .build();
    }

    @Test
    void generateTokenShouldContainUsername() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("test-user", jwtService.extractUsername(token));
    }

    @Test
    void isTokenValidShouldReturnTrueForMatchingUser() {
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValidShouldReturnFalseForExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject("test-user")
                .issuedAt(new Date(System.currentTimeMillis() - 2_000))
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        assertFalse(jwtService.isTokenValid(expiredToken, userDetails));
    }
}
