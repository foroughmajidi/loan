package com.fintech.loansystem.service.strategy;

import com.fintech.loansystem.enums.LoanType;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.math.BigDecimal;

@ControllerAdvice
public class BusinessLoanStrategy implements LoanStrategy, LoanTypeProvider {
    @Override
    public LoanType getLoanType() {
        return LoanType.BUSINESS;
    }

    @Override
    public BigDecimal calculateInterest(BigDecimal loanAmount) {
        return loanAmount.multiply(new BigDecimal("0.10")); // 10% interest rate
    }
}
