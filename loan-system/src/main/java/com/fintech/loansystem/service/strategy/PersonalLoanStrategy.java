package com.fintech.loansystem.service.strategy;

import com.fintech.loansystem.enums.LoanType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PersonalLoanStrategy implements LoanStrategy, LoanTypeProvider {
    @Override
    public LoanType getLoanType() {
        return LoanType.PERSONAL;
    }

    @Override
    public BigDecimal calculateInterest(BigDecimal loanAmount) {
        return loanAmount.multiply(new BigDecimal("0.12")); // 12% interest rate
    }
}

