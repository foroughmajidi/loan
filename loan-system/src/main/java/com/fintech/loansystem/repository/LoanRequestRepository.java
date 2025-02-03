package com.fintech.loansystem.repository;

import com.fintech.loansystem.enums.LoanStatus;
import com.fintech.loansystem.model.Loan;
import com.fintech.loansystem.model.LoanRequest;
import com.fintech.loansystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {
    Optional<LoanRequest> findFirstByUserAndLoanAndStatusIn(User user, Loan loan, List<LoanStatus> statusList);
}
