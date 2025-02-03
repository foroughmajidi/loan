package com.fintech.loansystem.service.strategy;

import com.fintech.loansystem.enums.LoanType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MortgageLoanStrategy implements LoanStrategy, LoanTypeProvider {
    @Override
    public LoanType getLoanType() {
        return LoanType.MORTGAGE;
    }

    @Override
    public BigDecimal calculateInterest(BigDecimal loanAmount) {
        return loanAmount.multiply(new BigDecimal("0.05")); // 5% interest rate
    }
}
