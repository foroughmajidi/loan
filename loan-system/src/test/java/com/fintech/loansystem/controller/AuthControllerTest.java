package com.fintech.loansystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.loansystem.dto.AuthenticationRequestDto;
import com.fintech.loansystem.dto.AuthenticationResponseDto;
import com.fintech.loansystem.dto.UserDto;
import com.fintech.loansystem.enums.Role;
import com.fintech.loansystem.model.User;
import com.fintech.loansystem.repository.LoanRequestRepository;
import com.fintech.loansystem.repository.UserRepository;
import com.fintech.loansystem.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtTokenProvider;

    private UserDto userDto;
    private AuthenticationRequestDto authRequestDto;
    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @BeforeEach
    void setUp() {
        loanRequestRepository.deleteAll();
        userRepository.deleteAll();

        userDto = UserDto.builder()
                .username("testuser")
                .password("password123")
                .role(Role.USER)
                .build();

        authRequestDto = new AuthenticationRequestDto("testuser", "password123");
    }

    @Test
    void registerUserSuccess() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn();

        UserDto responseDto = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        assertNotNull(responseDto);
        assertEquals(userDto.getUsername(), responseDto.getUsername());
        assertEquals(userDto.getRole(), responseDto.getRole());
        assertNotNull(responseDto.getPassword());
        User savedUser = userRepository.findByUsername(userDto.getUsername()).orElse(null);
        assertNotNull(savedUser);
        assertNotEquals(userDto.getPassword(), savedUser.getPassword()); // Password should be hashed
    }

    @Test
    void registerUserDuplicateUsername() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUserSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequestDto)))
                .andExpect(status().isOk())
                .andReturn();

        AuthenticationResponseDto responseDto = objectMapper.readValue(result.getResponse().getContentAsString(), AuthenticationResponseDto.class);
        assertNotNull(responseDto);
        assertNotNull(responseDto.getToken());

        assertTrue(jwtTokenProvider.validateToken(responseDto.getToken(), userDto.getUsername()));
    }

    @Test
    void loginUserInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        authRequestDto = new AuthenticationRequestDto("testuser", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerUserInvalidData() throws Exception {
        UserDto invalidUser = UserDto.builder().username("").password("").build();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUserMissingCredentials() throws Exception {
        AuthenticationRequestDto invalidRequest = new AuthenticationRequestDto("", "");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
