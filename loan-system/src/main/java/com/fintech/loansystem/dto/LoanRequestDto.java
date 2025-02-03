package com.fintech.loansystem.dto;

import com.fintech.loansystem.enums.LoanType;
import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Data

public class LoanRequestDto {
    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Amount is required")
    @Min(value = 100, message = "Minimum loan amount is 100")
    private BigDecimal amount;
}
