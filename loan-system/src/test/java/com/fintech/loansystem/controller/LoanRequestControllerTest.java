package com.fintech.loansystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LoanRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;  // Autowire JwtUtil

    private Loan testLoan;
    private User testUser;
    private User adminUser;
    private String userToken;
    private String adminToken;

    @BeforeEach
    public void setUp() {
        loanRepository.deleteAll();
        userRepository.deleteAll();
        loanRequestRepository.deleteAll();

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        userRepository.save(testUser);

        testLoan = new Loan();
        testLoan.setName("Home Loan");
        testLoan.setAmount(new BigDecimal("100000"));
        testLoan.setLoanType(LoanType.PERSONAL);
        testLoan.setStatus(LoanStatus.PENDING);
        testLoan.setInterest(new BigDecimal("5"));
        testLoan.setCreatedAt(LocalDateTime.now());
        loanRepository.save(testLoan);

        // Generate tokens
        userToken = jwtUtil.generateToken(testUser.getUsername(), testUser.getRole());
        adminToken = jwtUtil.generateToken(adminUser.getUsername(), adminUser.getRole());
    }

    @Test
    public void testRequestLoan() throws Exception {
        LoanRequestDto loanRequestDto = new LoanRequestDto();
        loanRequestDto.setAmount(new BigDecimal("100000"));
        loanRequestDto.setName("Home Loan");

        mockMvc.perform(post("/api/loan-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(objectMapper.writeValueAsString(loanRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(100000))
                .andExpect(jsonPath("$.loanName").value("Home Loan"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    public void testAcceptLoan() throws Exception {
        LoanRequest loanRequest = createTestLoanRequest();

        mockMvc.perform(put("/api/loan-requests/accept/{id}", loanRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanRequest.getId()))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }



    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testRejectLoan() throws Exception {
        LoanRequest loanRequest = createTestLoanRequest();

        mockMvc.perform(put("/api/loan-requests/reject/{id}", loanRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanRequest.getId()))
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testNonAdminCannotAcceptLoan() throws Exception {
        LoanRequest loanRequest = createTestLoanRequest();

        mockMvc.perform(put("/api/loan-requests/accept/{id}", loanRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testAcceptNonExistentLoan() throws Exception {
        mockMvc.perform(put("/api/loan-requests/accept/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private LoanRequest createTestLoanRequest() {
        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setUser(testUser);
        loanRequest.setLoan(testLoan);
        loanRequest.setAmount(new BigDecimal("100000"));
        loanRequest.setStatus(LoanStatus.PENDING);
        loanRequest.setCreateTime(LocalDateTime.now());
        return loanRequestRepository.save(loanRequest);
    }
}
