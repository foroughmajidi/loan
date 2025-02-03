package com.fintech.loansystem.service;

import com.fintech.loansystem.dto.LoanRequestDto;
import com.fintech.loansystem.dto.LoanResponseDto;
import com.fintech.loansystem.exception.LoanNotFoundException;
import com.fintech.loansystem.model.Loan;
import com.fintech.loansystem.repository.LoanRepository;
import com.fintech.loansystem.service.strategy.LoanStrategyFactory;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public LoanResponseDto createLoan(LoanRequestDto requestDto) {
        BigDecimal interest = loanStrategyFactory.getStrategy(requestDto.getLoanType())
                .calculateInterest(requestDto.getAmount());

        Loan loan = Loan.builder()
                .loanType(requestDto.getLoanType())
                .amount(requestDto.getAmount())
                .interest(interest)
                .createdAt(LocalDateTime.now())
                .build();

        loan = loanRepository.save(loan);

        return mapToDTO(loan);
    }

    public LoanResponseDto getLoanById(Long id) {
        Loan loan = loanRepository.findById(id)

                .orElseThrow(() -> new LoanNotFoundException("Loan with ID " + id + " not found"));

        return mapToDTO(loan);
    }

    public List<LoanResponseDto> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanResponseDto updateLoan(Long id, LoanRequestDto requestDto) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("Loan with ID " + id + " not found"));

        loan.setLoanType(requestDto.getLoanType());
        loan.setAmount(requestDto.getAmount());

        BigDecimal interest = loanStrategyFactory.getStrategy(requestDto.getLoanType())
                .calculateInterest(requestDto.getAmount());

        loan.setInterest(interest);

        loan = loanRepository.save(loan);

        return mapToDTO(loan);
    }

    @Transactional
    public void deleteLoan(Long id) {
        Loan loan = loanRepository.findById(id)

                .orElseThrow(() -> new LoanNotFoundException("Loan with ID " + id + " not found"));

        loanRepository.delete(loan);
    }

    private LoanResponseDto mapToDTO(Loan loan) {
        LoanResponseDto dto = new LoanResponseDto();
        dto.setId(loan.getId());
        dto.setLoanType(loan.getLoanType());
        dto.setAmount(loan.getAmount());
        dto.setInterest(loan.getInterest());
        dto.setCreatedAt(loan.getCreatedAt());
        return dto;
    }
}
