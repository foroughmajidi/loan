package com.fintech.loansystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.loansystem.dto.LoanDto;
import com.fintech.loansystem.enums.LoanType;
import com.fintech.loansystem.enums.Role;
import com.fintech.loansystem.model.Loan;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LoanRequestRepository loanRequestRepository;
    @Autowired
    private JwtUtil jwtTokenProvider;

    private LoanDto loanDto;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        loanRequestRepository.deleteAll();
        loanRepository.deleteAll();
        userRepository.deleteAll();
        loanDto = new LoanDto();
        loanDto.setName("Test Loan");
        loanDto.setAmount(BigDecimal.valueOf(1000));
        loanDto.setLoanType(LoanType.PERSONAL);

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        User user = new User();
        user.setUsername("user");
        user.setPassword("userpassword");
        user.setRole(Role.USER);
        userRepository.save(user);

        adminToken = jwtTokenProvider.generateToken(admin.getUsername(), admin.getRole());
        userToken = jwtTokenProvider.generateToken(user.getUsername(), user.getRole());

        loanRepository.deleteAll();
    }

    @Test
    void createLoanAsAdminSuccess() throws Exception {
        mockMvc.perform(post("/api/loans/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.loanType").value("PERSONAL"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.interest").value(120.00))
                .andExpect(jsonPath("$.name").value("Test Loan"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createLoanAsUserForbidden() throws Exception {
        mockMvc.perform(post("/api/loans/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLoanByIdSuccess() throws Exception {
        Loan savedLoan = loanRepository.save(Loan.builder()
                .name("Test Loan")
                .amount(BigDecimal.valueOf(1000))
                .loanType(LoanType.PERSONAL)
                .interest(BigDecimal.valueOf(5))
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/loans/" + savedLoan.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedLoan.getId()))
                .andExpect(jsonPath("$.name").value("Test Loan"));
    }

    @Test
    void getAllLoansSuccess() throws Exception {
        loanRepository.save(Loan.builder().name("Loan 1").amount(BigDecimal.valueOf(1000)).loanType(LoanType.PERSONAL).interest(BigDecimal.valueOf(5)).createdAt(LocalDateTime.now()).build());
        loanRepository.save(Loan.builder().name("Loan 2").amount(BigDecimal.valueOf(2000)).loanType(LoanType.BUSINESS).interest(BigDecimal.valueOf(7)).createdAt(LocalDateTime.now()).build());

        mockMvc.perform(get("/api/loans/findLoans")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Loan 1"))
                .andExpect(jsonPath("$[1].name").value("Loan 2"));
    }

    @Test
    void updateLoanAsAdminSuccess() throws Exception {
        Loan savedLoan = loanRepository.save(Loan.builder().name("Old Loan").amount(BigDecimal.valueOf(1000))
                .loanType(LoanType.PERSONAL).interest(BigDecimal.valueOf(6)).createdAt(LocalDateTime.now()).build());
        LoanDto updateDto = new LoanDto();
        updateDto.setName("Updated Loan");
        updateDto.setAmount(BigDecimal.valueOf(1500));
        updateDto.setLoanType(LoanType.BUSINESS);

        mockMvc.perform(put("/api/loans/updateLoan/" + savedLoan.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Loan"))
                .andExpect(jsonPath("$.amount").value(1500))
                .andExpect(jsonPath("$.loanType").value("BUSINESS"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.interest").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void deleteLoanAsAdminSuccess() throws Exception {
        Loan savedLoan = loanRepository.save(Loan.builder().name("To Delete").amount(BigDecimal.valueOf(1000)).loanType(LoanType.PERSONAL).interest(BigDecimal.valueOf(5)).createdAt(LocalDateTime.now()).build());

        mockMvc.perform(delete("/api/loans/deleteLoan/" + savedLoan.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getAllLoanPlansSuccess() throws Exception {
        loanRepository.save(Loan.builder().name("Loan Plan 1").amount(BigDecimal.valueOf(1000)).loanType(LoanType.PERSONAL).interest(BigDecimal.valueOf(5)).createdAt(LocalDateTime.now()).build());
        loanRepository.save(Loan.builder().name("Loan Plan 2").amount(BigDecimal.valueOf(2000)).loanType(LoanType.BUSINESS).interest(BigDecimal.valueOf(7)).createdAt(LocalDateTime.now()).build());

        mockMvc.perform(get("/api/loans/loanNames")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Loan Plan 1"))
                .andExpect(jsonPath("$[1]").value("Loan Plan 2"));
    }
}
