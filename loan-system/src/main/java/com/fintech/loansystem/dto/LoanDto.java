package com.fintech.loansystem.dto;

import com.fintech.loansystem.enums.LoanType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanDto {

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Loan amount must be greater than or equal to 0") // Ensures the amount is >= 0
    private BigDecimal amount;

    @NotBlank(message = "Name is required")
    private String name;

}
