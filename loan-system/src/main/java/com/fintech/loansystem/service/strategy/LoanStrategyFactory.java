package com.fintech.loansystem.service.strategy;

import com.fintech.loansystem.enums.LoanType;
import com.fintech.loansystem.exception.LoanTypeNotSupportedException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LoanStrategyFactory {
    private final Map<LoanType, LoanStrategy> loanStrategyMap;

    public LoanStrategyFactory(Set<LoanTypeProvider> typeProviders, Set<LoanStrategy> strategies) {
        /* Map LoanType to LoanStrategy dynamically */
        this.loanStrategyMap = typeProviders.stream()
                .collect(Collectors.toMap(LoanTypeProvider::getLoanType, provider ->
                        strategies.stream()
                                .filter(strategy -> provider.getClass().isAssignableFrom(strategy.getClass()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("No strategy found for " + provider.getLoanType()))
                ));

    }

    public LoanStrategy getStrategy(LoanType loanType) {
        if (!loanStrategyMap.containsKey(loanType)) {
            throw new LoanTypeNotSupportedException("Loan type " + loanType + " is not supported");
        }
        return loanStrategyMap.get(loanType);
    }
}
