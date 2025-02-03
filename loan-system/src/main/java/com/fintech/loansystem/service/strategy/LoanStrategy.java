package com.fintech.loansystem.service.strategy;

import java.math.BigDecimal;

public interface LoanStrategy {
    BigDecimal calculateInterest(BigDecimal loanAmount);
}
