package com.fintech.loansystem.service;

import com.fintech.loansystem.dto.LoanReqResponseDto;
import com.fintech.loansystem.dto.LoanRequestDto;
import com.fintech.loansystem.enums.LoanStatus;
import com.fintech.loansystem.mapper.DtoMapper;
import com.fintech.loansystem.model.Loan;
import com.fintech.loansystem.model.LoanRequest;
import com.fintech.loansystem.model.User;
import com.fintech.loansystem.repository.LoanRepository;
import com.fintech.loansystem.repository.LoanRequestRepository;
import com.fintech.loansystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanRequestService {
    private final LoanRepository loanRepository;
    private final LoanRequestRepository loanRequestRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    @Transactional
    public LoanReqResponseDto requestLoan(LoanRequestDto loanRequestDto) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Loan loan = loanRepository.findByName(loanRequestDto.getName())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        LoanRequest existingLoanRequest = loanRequestRepository.findFirstByUserAndLoanAndStatusIn(
                        user, loan, List.of(LoanStatus.PENDING, LoanStatus.REJECTED))
                .orElse(null);

        if (existingLoanRequest != null) {
            throw new RuntimeException("You already have a pending or rejected loan request for this loan.");
        }

        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setUser(user);
        loanRequest.setLoan(loan);
        loanRequest.setAmount(loanRequestDto.getAmount());
        loanRequest.setStatus(LoanStatus.PENDING);
        loanRequest.setCreateTime(LocalDateTime.now());

        loanRequest = loanRequestRepository.save(loanRequest);

        return dtoMapper.loanRequestToLoanResponseDto(loanRequest);
    }

    @Transactional
    public LoanReqResponseDto cancelRequest(Long loanRequestId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LoanRequest loanRequest = loanRequestRepository.findById(loanRequestId)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        if (!loanRequest.getUser().equals(user)) {
            throw new RuntimeException("You can only cancel your own loan request.");
        }

        if (!loanRequest.getStatus().equals(LoanStatus.PENDING)) {
            throw new RuntimeException("Only pending loan requests can be canceled.");
        }

        loanRequest.setStatus(LoanStatus.CANCELED);

        loanRequest = loanRequestRepository.save(loanRequest);

        return dtoMapper.loanRequestToLoanResponseDto(loanRequest);
    }

}
