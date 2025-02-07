package com.fintech.loansystem.service;

import com.fintech.loansystem.dto.LoanReqResponseDto;
import com.fintech.loansystem.dto.LoanRequestDto;
import com.fintech.loansystem.enums.LoanStatus;
import com.fintech.loansystem.exception.*;
import com.fintech.loansystem.mapper.DtoMapper;
import com.fintech.loansystem.model.Loan;
import com.fintech.loansystem.model.LoanRequest;
import com.fintech.loansystem.model.User;
import com.fintech.loansystem.repository.LoanRepository;
import com.fintech.loansystem.repository.LoanRequestRepository;
import com.fintech.loansystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanRequestServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanRequestRepository loanRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DtoMapper dtoMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private LoanRequestService loanRequestService;

    private User user;
    private Loan loan;
    private LoanRequest loanRequest;
    private LoanRequestDto loanRequestDto;
    private LoanReqResponseDto loanReqResponseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testUser");

        loan = new Loan();
        loan.setName("TestLoan");
        loan.setAmount(BigDecimal.valueOf(1000));

        loanRequest = new LoanRequest();
        loanRequest.setId(1L);
        loanRequest.setUser(user);
        loanRequest.setLoan(loan);
        loanRequest.setAmount(BigDecimal.valueOf(1000));
        loanRequest.setStatus(LoanStatus.PENDING);
        loanRequest.setCreateTime(LocalDateTime.now());

        loanRequestDto = new LoanRequestDto();
        loanRequestDto.setName("TestLoan");
        loanRequestDto.setAmount(BigDecimal.valueOf(1000));

        loanReqResponseDto = new LoanReqResponseDto();
        loanReqResponseDto.setId(1L);
        loanReqResponseDto.setStatus(LoanStatus.PENDING);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testUser");
    }

    @Test
    void requestLoan_Success() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(loanRepository.findByName("TestLoan")).thenReturn(Optional.of(loan));
        when(loanRequestRepository.findFirstByUserAndLoanAndStatusIn(any(), any(), any())).thenReturn(Optional.empty());
        when(loanRequestRepository.save(any(LoanRequest.class))).thenReturn(loanRequest);
        when(dtoMapper.loanRequestToLoanResponseDto(any(LoanRequest.class))).thenReturn(loanReqResponseDto);

        LoanReqResponseDto result = loanRequestService.requestLoan(loanRequestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(LoanStatus.PENDING, result.getStatus());
        verify(loanRequestRepository).save(any(LoanRequest.class));
    }

    @Test
    void requestLoan_UserNotFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> loanRequestService.requestLoan(loanRequestDto));
    }

    @Test
    void requestLoan_LoanNotFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(loanRepository.findByName("TestLoan")).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> loanRequestService.requestLoan(loanRequestDto));
    }

    @Test
    void requestLoan_AmountMismatch() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(loanRepository.findByName("TestLoan")).thenReturn(Optional.of(loan));
        loanRequestDto.setAmount(BigDecimal.valueOf(2000));

        assertThrows(LoanAmountOutOfRangeException.class, () -> loanRequestService.requestLoan(loanRequestDto));
    }

    @Test
    void requestLoan_ExistingRequest() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(loanRepository.findByName("TestLoan")).thenReturn(Optional.of(loan));
        when(loanRequestRepository.findFirstByUserAndLoanAndStatusIn(any(), any(), any())).thenReturn(Optional.of(loanRequest));

        assertThrows(LoanRequestAlreadyExistsException.class, () -> loanRequestService.requestLoan(loanRequestDto));
    }

    @Test
    void cancelRequest_Success() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(loanRequestRepository.findById(1L)).thenReturn(Optional.of(loanRequest));
        when(loanRequestRepository.save(any(LoanRequest.class))).thenReturn(loanRequest);
        when(dtoMapper.loanRequestToLoanResponseDto(any(LoanRequest.class))).thenReturn(loanReqResponseDto);

        LoanReqResponseDto result = loanRequestService.cancelRequest(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(LoanStatus.PENDING, result.getStatus());
        verify(loanRequestRepository).save(any(LoanRequest.class));
    }

    @Test
    void cancelRequest_UserNotFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> loanRequestService.cancelRequest(1L));
    }

    @Test
    void cancelRequest_LoanRequestNotFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(loanRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> loanRequestService.cancelRequest(1L));
    }

    @Test
    void cancelRequest_UnauthorizedUser() {
        User otherUser = new User();
        otherUser.setUsername("otherUser");
        loanRequest.setUser(otherUser);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(loanRequestRepository.findById(1L)).thenReturn(Optional.of(loanRequest));

        assertThrows(LoanRequestAuthorizationException.class, () -> loanRequestService.cancelRequest(1L));
    }

    @Test
    void cancelRequest_InvalidStatus() {
        loanRequest.setStatus(LoanStatus.APPROVED);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(loanRequestRepository.findById(1L)).thenReturn(Optional.of(loanRequest));

        assertThrows(InvalidLoanRequestStatusException.class, () -> loanRequestService.cancelRequest(1L));
    }
}
