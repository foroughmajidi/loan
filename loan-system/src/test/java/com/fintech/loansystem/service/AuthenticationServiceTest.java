package com.fintech.loansystem.service;

import com.fintech.loansystem.dto.AuthenticationRequestDto;
import com.fintech.loansystem.dto.AuthenticationResponseDto;
import com.fintech.loansystem.enums.Role;
import com.fintech.loansystem.model.User;
import com.fintech.loansystem.repository.UserRepository;
import com.fintech.loansystem.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AuthenticationRequestDto validRequest;
    private User validUser;

    @BeforeEach
    void setUp() {

        validRequest = new AuthenticationRequestDto("testuser", "password");
        validUser = new User();
        validUser.setUsername("testuser");
        validUser.setRole(Role.USER);
    }

    @Test
    void authenticateValidCredentialsReturnsToken() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsername(validRequest.getUsername())).thenReturn(Optional.of(validUser));
        when(jwtUtil.generateToken(validRequest.getUsername(), validUser.getRole())).thenReturn("valid.jwt.token");

        AuthenticationResponseDto response = authenticationService.authenticate(validRequest);

        assertNotNull(response);
        assertEquals("valid.jwt.token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername(validRequest.getUsername());
        verify(jwtUtil).generateToken(validRequest.getUsername(), validUser.getRole());
    }

    @Test
    void authenticateInvalidCredentialsThrowsBadCredentialsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(validRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userRepository, jwtUtil);
    }

    @Test
    void authenticateUserNotFoundThrowsUsernameNotFoundException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsername(validRequest.getUsername())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(validRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername(validRequest.getUsername());
        verifyNoInteractions(jwtUtil);
    }


}
