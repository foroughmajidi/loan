package com.fintech.loansystem.service;

import com.fintech.loansystem.dto.LoanDto;
import com.fintech.loansystem.dto.LoanResponseDto;
import com.fintech.loansystem.enums.LoanStatus;
import com.fintech.loansystem.enums.LoanType;
import com.fintech.loansystem.exception.LoanNotFoundException;
import com.fintech.loansystem.mapper.DtoMapper;
import com.fintech.loansystem.model.Loan;
import com.fintech.loansystem.repository.LoanRepository;
import com.fintech.loansystem.service.strategy.LoanStrategy;
import com.fintech.loansystem.service.strategy.LoanStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanStrategyFactory loanStrategyFactory;

    @Mock
    private DtoMapper dtoMapper;

    @Mock
    private LoanStrategy loanStrategy;

    @InjectMocks
    private LoanService loanService;

    private LoanDto loanDto;
    private Loan loan;
    private LoanResponseDto loanResponseDto;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();

        LocalDateTime fixedDateTime = LocalDateTime.of(2023, 1, 1, 12, 0);

        loanDto = new LoanDto(LoanType.PERSONAL, BigDecimal.valueOf(1000), "Test Loan");

        loan = Loan.builder()
                .id(1L)
                .loanType(LoanType.PERSONAL)
                .amount(BigDecimal.valueOf(1000))
                .interest(BigDecimal.valueOf(50))
                .createdAt(fixedDateTime)
                .name("Test Loan")
                .status(LoanStatus.PENDING)
                .build();

        loanResponseDto = new LoanResponseDto();
        loanResponseDto.setId(1L);
        loanResponseDto.setLoanType(LoanType.PERSONAL);
        loanResponseDto.setAmount(BigDecimal.valueOf(1000));
        loanResponseDto.setInterest(BigDecimal.valueOf(50));
        loanResponseDto.setCreatedAt(fixedDateTime);
        loanResponseDto.setName("Test Loan");
    }

    @Test
    void createLoanSuccess() {
        when(loanStrategyFactory.getStrategy(any())).thenReturn(loanStrategy);
        when(loanStrategy.calculateInterest(any())).thenReturn(BigDecimal.valueOf(50));
        when(loanRepository.save(any())).thenReturn(loan);
        when(dtoMapper.loanToLoanResponseDto(any())).thenReturn(loanResponseDto);

        LoanResponseDto result = loanService.createLoan(loanDto);

        assertNotNull(result);
        assertEquals(loanResponseDto, result);
        verify(loanRepository).save(any());
    }

    @Test
    void getLoanByIdSuccess() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(dtoMapper.loanToLoanResponseDto(loan)).thenReturn(loanResponseDto);

        LoanResponseDto result = loanService.getLoanById(1L);

        assertNotNull(result);
        assertEquals(loanResponseDto, result);
    }

    @Test
    void getLoanByIdThrowsLoanNotFoundException() {
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> loanService.getLoanById(1L));
    }

    @Test
    void getAllLoansSuccess() {
        List<Loan> loans = Arrays.asList(loan, loan);
        when(loanRepository.findAll()).thenReturn(loans);
        when(dtoMapper.loanToLoanResponseDto(any())).thenReturn(loanResponseDto);

        List<LoanResponseDto> result = loanService.getAllLoans();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void updateLoanSuccess() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanStrategyFactory.getStrategy(any())).thenReturn(loanStrategy);
        when(loanStrategy.calculateInterest(any())).thenReturn(BigDecimal.valueOf(60));
        when(loanRepository.save(any())).thenReturn(loan);
        when(dtoMapper.loanToLoanResponseDto(any())).thenReturn(loanResponseDto);

        LoanResponseDto result = loanService.updateLoan(1L, loanDto);

        assertNotNull(result);
        assertEquals(loanResponseDto, result);
        verify(loanRepository).save(any());
    }

    @Test
    void deleteLoanSuccess() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertDoesNotThrow(() -> loanService.deleteLoan(1L));
        verify(loanRepository).delete(loan);
    }

    @Test
    void acceptLoanSuccess() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any())).thenReturn(loan);
        when(dtoMapper.loanToLoanResponseDto(any())).thenReturn(loanResponseDto);

        LoanResponseDto result = loanService.acceptLoan(1L);

        assertNotNull(result);
        assertEquals(LoanStatus.APPROVED, loan.getStatus());
    }


    @Test
    void rejectLoanSuccess() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any())).thenReturn(loan);
        when(dtoMapper.loanToLoanResponseDto(any())).thenReturn(loanResponseDto);

        LoanResponseDto result = loanService.rejectLoan(1L);

        assertNotNull(result);
        assertEquals(LoanStatus.REJECTED, loan.getStatus());
    }


}
