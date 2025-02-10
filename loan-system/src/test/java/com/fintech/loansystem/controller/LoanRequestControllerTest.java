package com.fintech.loansystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.loansystem.dto.LoanReqResponseDto;
import com.fintech.loansystem.dto.LoanRequestDto;
import com.fintech.loansystem.enums.LoanStatus;
import com.fintech.loansystem.enums.LoanType;
import com.fintech.loansystem.enums.Role;
import com.fintech.loansystem.model.Loan;
import com.fintech.loansystem.model.LoanRequest;
import com.fintech.loansystem.model.User;
import com.fintech.loansystem.repository.LoanRepository;
import com.fintech.loansystem.repository.LoanRequestRepository;
import com.fintech.loansystem.repository.UserRepository;
import com.fintech.loansystem.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoanRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @Autowired
    private JwtUtil jwtTokenProvider;

    private User regularUser;
    private Loan loan;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        loanRequestRepository.deleteAll();
        loanRepository.deleteAll();
        userRepository.deleteAll();

        User adminUser = User.builder()
                .username("admin")
                .password("password")
                .role(Role.ADMIN)
                .build();
        userRepository.save(adminUser);

        regularUser = User.builder()
                .username("user")
                .password("password")
                .role(Role.USER)
                .build();
        userRepository.save(regularUser);

        loan = Loan.builder()
                .loanType(LoanType.PERSONAL) // Add appropriate LoanType if needed
                .amount(BigDecimal.valueOf(5000))
                .name("Test Loan")
                .interest(BigDecimal.valueOf(5.0))
                .createdAt(LocalDateTime.now())
                .build();
        loanRepository.save(loan);

        adminToken = jwtTokenProvider.generateToken(adminUser.getUsername(), adminUser.getRole());
        userToken = jwtTokenProvider.generateToken(regularUser.getUsername(), regularUser.getRole());
    }


    @Test
    @DisplayName("POST /api/loan-requests/requestLoan - Request a loan successfully")
    void requestLoanShouldCreateLoanRequest() throws Exception {
        loanRequestRepository.deleteAll();
        loanRepository.deleteAll();
        loanRepository.save(Loan.builder()
                .name("Test Loan")
                .amount(BigDecimal.valueOf(1000))
                .loanType(LoanType.PERSONAL)
                .interest(BigDecimal.valueOf(5))
                .createdAt(LocalDateTime.now())
                .build());
        LoanRequestDto loanRequestDto = new LoanRequestDto();
        loanRequestDto.setName("Test Loan");
        loanRequestDto.setAmount(BigDecimal.valueOf(1000));

        MvcResult result = mockMvc.perform(post("/api/loan-requests/requestLoan")
                        .header("Authorization", "Bearer " + userToken)
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
    @DisplayName("PUT /api/loan-requests/cancelLoanRequest/{id} - Cancel a loan request successfully")
    void cancelLoanRequestShouldCancelLoanSuccessfully() throws Exception {
        LoanRequest loanRequest = LoanRequest.builder()
                .user(regularUser)
                .amount(BigDecimal.valueOf(5000))
                .status(LoanStatus.PENDING)
                .loan(loan)
                .createTime(LocalDateTime.now())
                .build();
        loanRequest = loanRequestRepository.save(loanRequest);

        mockMvc.perform(put("/api/loan-requests/cancelLoanRequest/{id}", loanRequest.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanRequest.getId()))
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    @DisplayName("PUT /api/loan-requests/accept/{loanRequestId} - Accept a loan request successfully (Admin only)")
    void acceptLoanRequestShouldAcceptLoanSuccessfully() throws Exception {
        LoanRequest loanRequest = LoanRequest.builder()
                .user(regularUser)
                .amount(BigDecimal.valueOf(5000))
                .status(LoanStatus.PENDING)
                .loan(loan)
                .createTime(LocalDateTime.now())
                .build();
        loanRequest = loanRequestRepository.save(loanRequest);

        mockMvc.perform(put("/api/loan-requests/accept/{loanRequestId}", loanRequest.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanRequest.getId()))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("PUT /api/loan-requests/reject/{loanRequestId} - Reject a loan request successfully (Admin only)")
    void rejectLoanRequestShouldRejectLoanSuccessfully() throws Exception {
        LoanRequest loanRequest = LoanRequest.builder()
                .user(regularUser)
                .amount(BigDecimal.valueOf(5000))
                .status(LoanStatus.PENDING)
                .loan(loan)
                .createTime(LocalDateTime.now())
                .build();
        loanRequest = loanRequestRepository.save(loanRequest);

        mockMvc.perform(put("/api/loan-requests/reject/{loanRequestId}", loanRequest.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanRequest.getId()))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @DisplayName("PUT /api/loan-requests/accept/{loanRequestId} - Forbidden for non-admin users")
    void acceptLoanShouldReturnForbiddenForNonAdmin() throws Exception {
        LoanRequest loanRequest = LoanRequest.builder()
                .user(regularUser)
                .amount(BigDecimal.valueOf(5000))
                .status(LoanStatus.PENDING)
                .loan(loan)
                .createTime(LocalDateTime.now())
                .build();
        loanRequest = loanRequestRepository.save(loanRequest);
        mockMvc.perform(put("/api/loan-requests/accept/{id}", loanRequest.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
