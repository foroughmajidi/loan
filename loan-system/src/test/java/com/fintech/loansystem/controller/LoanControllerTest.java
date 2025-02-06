package com.fintech.loansystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.loansystem.dto.LoanDto;
import com.fintech.loansystem.dto.LoanResponseDto;
import com.fintech.loansystem.enums.LoanType;
import com.fintech.loansystem.enums.Role;
import com.fintech.loansystem.model.User;
import com.fintech.loansystem.repository.LoanRepository;
import com.fintech.loansystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    private LoanDto loanDto;

    @BeforeEach
    void setUp() {


        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);

        loanDto = new LoanDto();
        loanDto.setLoanType(LoanType.PERSONAL);
        loanDto.setAmount(BigDecimal.valueOf(1000));
        loanDto.setName("Test Loan " + System.currentTimeMillis());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createLoanShouldCreateNewLoan() throws Exception {
        mockMvc.perform(post("/api/loans/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanType").value("PERSONAL"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.name").value(loanDto.getName()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getLoanByIdShouldReturnLoan() throws Exception {
        LoanResponseDto createdLoan = createTestLoan();

        mockMvc.perform(get("/api/loans/{id}", createdLoan.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdLoan.getId()))
                .andExpect(jsonPath("$.loanType").value("PERSONAL"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.name").value(createdLoan.getName()));
    }

    @Test
    void getAllLoansShouldReturnAllLoans() throws Exception {
        createTestLoan();

        mockMvc.perform(get("/api/loans/findLoans"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].loanType", everyItem(is("PERSONAL"))))
                .andExpect(jsonPath("$[*].amount", everyItem(is(1000))));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateLoanShouldUpdateExistingLoan() throws Exception {
        LoanResponseDto createdLoan = createTestLoan();

        LoanDto updatedLoanDto = new LoanDto();
        updatedLoanDto.setLoanType(LoanType.BUSINESS);
        updatedLoanDto.setAmount(BigDecimal.valueOf(2000));
        updatedLoanDto.setName("Updated Loan");

        mockMvc.perform(put("/api/loans/updateLoan/{id}", createdLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLoanDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdLoan.getId()))
                .andExpect(jsonPath("$.loanType").value("BUSINESS"))
                .andExpect(jsonPath("$.amount").value(2000))
                .andExpect(jsonPath("$.name").value("Updated Loan"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteLoanShouldDeleteExistingLoan() throws Exception {
        LoanResponseDto createdLoan = createTestLoan();

        mockMvc.perform(delete("/api/loans/deleteLoan/{id}", createdLoan.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/loans/{id}", createdLoan.getId()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private LoanResponseDto createTestLoan() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/loans/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Create Loan Response: " + responseContent);

        return objectMapper.readValue(responseContent, LoanResponseDto.class);
    }
}
