package com.fintech.loansystem.service;

import com.fintech.loansystem.dto.AuthenticationRequestDto;
import com.fintech.loansystem.dto.AuthenticationResponseDto;
import com.fintech.loansystem.enums.Role;
import com.fintech.loansystem.model.User;
import com.fintech.loansystem.repository.UserRepository;
import com.fintech.loansystem.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTest {

    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private UserRepository userRepository;
    private AuthenticationService authenticationService;

    @BeforeEach
    public void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtUtil = mock(JwtUtil.class);
        userRepository = mock(UserRepository.class);
        authenticationService = new AuthenticationService(authenticationManager, jwtUtil, userRepository);
    }

    @Test
    public void testAuthenticateValidCredentials() {
        AuthenticationRequestDto requestDto = new AuthenticationRequestDto("username", "password");
        User user = User.builder()
                .username("username")
                .password("password")
                .role(Role.USER)
                .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);
        when(userRepository.findByUsername("username"))
                .thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("username", Role.USER))
                .thenReturn("jwt-token");

        AuthenticationResponseDto responseDto = authenticationService.authenticate(requestDto);

        assertNotNull(responseDto);
        assertEquals("jwt-token", responseDto.getToken());
    }

    @Test
    public void testAuthenticateInvalidCredentials() {
        AuthenticationRequestDto requestDto = new AuthenticationRequestDto("invalidUsername", "invalidPassword");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(requestDto));
    }

    @Test
    public void testAuthenticateUserNotFound() {
        AuthenticationRequestDto requestDto = new AuthenticationRequestDto("nonExistingUser", "password");
        when(authenticationManager.authenticate(any()))
                .thenReturn(null);
        when(userRepository.findByUsername("nonExistingUser"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(requestDto));
    }
}