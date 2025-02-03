package com.fintech.loansystem.dto;

import com.fintech.loansystem.enums.LoanStatus;
import com.fintech.loansystem.model.Loan;
import com.fintech.loansystem.model.User;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data

public class LoanReqResponseDto {
    private Long id;
    private User user;
    private BigDecimal amount;
    private LoanStatus status;
    private LocalDateTime createTime;
    private Loan loan;
}
