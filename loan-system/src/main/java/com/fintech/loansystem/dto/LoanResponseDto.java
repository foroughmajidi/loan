package com.fintech.loansystem.dto;

import com.fintech.loansystem.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponseDto {
    private Long id;
    private LoanType loanType;
    private BigDecimal amount;
    private BigDecimal interest;
    private LocalDateTime createdAt;
    private String name;
}
