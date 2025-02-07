package com.fintech.loansystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.loansystem.dto.LoanReqResponseDto;
import com.fintech.loansystem.dto.LoanRequestDto;
import com.fintech.loansystem.enums.LoanStatus;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LoanRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private JwtUtil jwtTokenProvider;

    private String userJwtToken;
    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @BeforeEach
    void setUp() {


        User regularUser = new User();
        regularUser.setUsername("regularuser");
        regularUser.setPassword("userpassword");
        regularUser.setRole(Role.USER);
        userRepository.save(regularUser);

        // Generate JWT tokens for each user
        userJwtToken = jwtTokenProvider.generateToken(regularUser.getUsername(), regularUser.getRole());
    }

    @Test
    void requestLoanSuccess() throws Exception {
        LoanRequestDto loanRequestDto = new LoanRequestDto();
        loanRequestDto.setName("Test Loan");
        loanRequestDto.setAmount(BigDecimal.valueOf(1000));

        MvcResult result = mockMvc.perform(post("/api/loan-requests")
                        .header("Authorization", "Bearer " + userJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        LoanReqResponseDto responseDto = objectMapper.readValue(result.getResponse().getContentAsString(), LoanReqResponseDto.class);
        assertNotNull(responseDto);
        assertEquals(LoanStatus.PENDING, responseDto.getStatus());
    }



    @Test
    void cancelLoanRequestSuccess() throws Exception {
        loanRequestRepository.deleteAll();
        LoanRequestDto loanRequestDto = new LoanRequestDto();
        loanRequestDto.setName("Test Loan");
        loanRequestDto.setAmount(BigDecimal.valueOf(1000));

        MvcResult createResult = mockMvc.perform(post("/api/loan-requests")
                        .header("Authorization", "Bearer " + userJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequestDto)))
                .andExpect(status().isOk())
                .andReturn();

        LoanReqResponseDto createdLoan = objectMapper.readValue(createResult.getResponse().getContentAsString(), LoanReqResponseDto.class);

        MvcResult cancelResult = mockMvc.perform(put("/api/loan-requests/cancelLoanRequest/" + createdLoan.getId())
                        .header("Authorization", "Bearer " + userJwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        LoanReqResponseDto canceledLoan = objectMapper.readValue(cancelResult.getResponse().getContentAsString(), LoanReqResponseDto.class);
        assertNotNull(canceledLoan);
        assertEquals(LoanStatus.CANCELED, canceledLoan.getStatus());
    }

    @Test
    void cancelLoanRequestNotFound() throws Exception {
        mockMvc.perform(put("/api/loan-requests/cancelLoanRequest/999")
                        .header("Authorization", "Bearer " + userJwtToken))
                .andExpect(status().isNotFound());
    }



}
