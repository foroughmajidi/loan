package com.fintech.loansystem.service;

import com.fintech.loansystem.dto.LoanDto;
import com.fintech.loansystem.dto.LoanResponseDto;
import com.fintech.loansystem.enums.LoanStatus;
import com.fintech.loansystem.enums.Role;
import com.fintech.loansystem.exception.LoanNotFoundException;
import com.fintech.loansystem.mapper.DtoMapper;
import com.fintech.loansystem.model.Loan;
import com.fintech.loansystem.model.User;
import com.fintech.loansystem.repository.LoanRepository;
import com.fintech.loansystem.repository.UserRepository;
import com.fintech.loansystem.service.strategy.LoanStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanStrategyFactory loanStrategyFactory;
    private final DtoMapper dtoMapper;
    private final UserRepository userRepository;


    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public LoanResponseDto createLoan(LoanDto loanDto) {
        BigDecimal interest = loanStrategyFactory.getStrategy(loanDto.getLoanType())
                .calculateInterest(loanDto.getAmount());


        Loan loan = Loan.builder()
                .loanType(loanDto.getLoanType())
                .amount(loanDto.getAmount())
                .interest(interest)
                .createdAt(LocalDateTime.now())
                .name(loanDto.getName())
                .status(LoanStatus.PENDING)
                .build();

        loan = loanRepository.save(loan);

        return dtoMapper.loanToLoanResponseDto(loan);
    }

    public LoanResponseDto getLoanById(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("Loan with ID " + id + " not found"));

        return dtoMapper.loanToLoanResponseDto(loan);
    }

    public List<LoanResponseDto> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(dtoMapper::loanToLoanResponseDto)
                .collect(Collectors.toList());
    }

    public List<String> getAllLoanName() {
        List<Loan> loan = loanRepository.findAll();
        return loan.stream()
                .map(Loan::getName)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanResponseDto updateLoan(Long id, LoanDto loanDto) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("Loan with ID " + id + " not found"));

        loan.setLoanType(loanDto.getLoanType());
        loan.setAmount(loanDto.getAmount());
        loan.setName(loanDto.getName());

        BigDecimal interest = loanStrategyFactory.getStrategy(loanDto.getLoanType())
                .calculateInterest(loanDto.getAmount());

        loan.setInterest(interest);
        loan = loanRepository.save(loan);
        return dtoMapper.loanToLoanResponseDto(loan);
    }

    @Transactional
    public void deleteLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("Loan with ID " + id + " not found"));

        loanRepository.delete(loan);
    }

    @Transactional
    public LoanResponseDto acceptLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!isAdmin()) {
            throw new RuntimeException("You are not authorized to accept this loan.");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loanRepository.save(loan);

        return dtoMapper.loanToLoanResponseDto(loan);
    }

    @Transactional
    public LoanResponseDto rejectLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!isAdmin()) {
            throw new RuntimeException("You are not authorized to reject this loan.");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loanRepository.save(loan);

        return dtoMapper.loanToLoanResponseDto(loan);
    }

    private boolean isAdmin() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getRole() == Role.ADMIN;
    }


}
