package com.fintech.loansystem.service;

import com.fintech.loansystem.dto.AuthenticationRequestDto;
import com.fintech.loansystem.dto.AuthenticationResponseDto;
import com.fintech.loansystem.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthenticationResponseDto authenticate(AuthenticationRequestDto requestDto) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword()));

        if (authentication.isAuthenticated()) {
            String token = jwtUtil.generateToken(requestDto.getUsername());
            return new AuthenticationResponseDto(token);
        } else {
            throw new AuthenticationException("Invalid credentials");

        }
    }
}
