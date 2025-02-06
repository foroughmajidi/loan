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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    private LoanRequestDto loanRequestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testUser");

        loan = new Loan();
        loan.setName("TestLoan");
        loan.setAmount(BigDecimal.valueOf(1000));

        loanRequestDto = new LoanRequestDto();
        loanRequestDto.setName("TestLoan");
        loanRequestDto.setAmount(BigDecimal.valueOf(1000));

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testUser");
    }

    @Nested
    @DisplayName("Request Loan Tests")
    class RequestLoanTests {

        @Test
        @DisplayName("Should successfully request a loan")
        void requestLoan_Success() {
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
            when(loanRepository.findByName("TestLoan")).thenReturn(Optional.of(loan));
            when(loanRequestRepository.findFirstByUserAndLoanAndStatusIn(any(), any(), any())).thenReturn(Optional.empty());
            when(loanRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(dtoMapper.loanRequestToLoanResponseDto(any())).thenReturn(new LoanReqResponseDto());

            LoanReqResponseDto result = loanRequestService.requestLoan(loanRequestDto);

            assertNotNull(result);
            verify(loanRequestRepository).save(any());
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void requestLoanUserNotFound() {
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> loanRequestService.requestLoan(loanRequestDto));
        }

        @Test
        @DisplayName("Should throw LoanNotFoundException when loan not found")
        void requestLoanLoanNotFound() {
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
            when(loanRepository.findByName("TestLoan")).thenReturn(Optional.empty());

            assertThrows(LoanNotFoundException.class, () -> loanRequestService.requestLoan(loanRequestDto));
        }

        @Test
        @DisplayName("Should throw LoanAmountOutOfRangeException when amount is incorrect")
        void requestLoanAmountOutOfRange() {
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
            when(loanRepository.findByName("TestLoan")).thenReturn(Optional.of(loan));
            loanRequestDto.setAmount(BigDecimal.valueOf(2000));

            assertThrows(LoanAmountOutOfRangeException.class, () -> loanRequestService.requestLoan(loanRequestDto));
        }

        @Test
        @DisplayName("Should throw LoanRequestAlreadyExistsException when request already exists")
        void requestLoanRequestAlreadyExists() {
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
            when(loanRepository.findByName("TestLoan")).thenReturn(Optional.of(loan));
            when(loanRequestRepository.findFirstByUserAndLoanAndStatusIn(any(), any(), any())).thenReturn(Optional.of(new LoanRequest()));

            assertThrows(LoanRequestAlreadyExistsException.class, () -> loanRequestService.requestLoan(loanRequestDto));
        }
    }

    @Nested
    @DisplayName("Cancel Request Tests")
    class CancelRequestTests {

        @Test
        @DisplayName("Should successfully cancel a loan request")
        void cancelRequest_Success() {
            LoanRequest loanRequest = new LoanRequest();
            loanRequest.setUser(user);
            loanRequest.setStatus(LoanStatus.PENDING);

            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
            when(loanRequestRepository.findById(1L)).thenReturn(Optional.of(loanRequest));
            when(loanRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(dtoMapper.loanRequestToLoanResponseDto(any())).thenReturn(new LoanReqResponseDto());

            LoanReqResponseDto result = loanRequestService.cancelRequest(1L);

            assertNotNull(result);
            assertEquals(LoanStatus.CANCELED, loanRequest.getStatus());
            verify(loanRequestRepository).save(loanRequest);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void cancelRequestUserNotFound() {
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> loanRequestService.cancelRequest(1L));
        }

        @Test
        @DisplayName("Should throw LoanNotFoundException when loan request not found")
        void cancelRequestLoanRequestNotFound() {
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
            when(loanRequestRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(LoanNotFoundException.class, () -> loanRequestService.cancelRequest(1L));
        }

        @Test
        @DisplayName("Should throw LoanRequestAuthorizationException when user is not authorized")
        void cancelRequestUnauthorized() {
            User otherUser = new User();
            otherUser.setUsername("otherUser");

            LoanRequest loanRequest = new LoanRequest();
            loanRequest.setUser(otherUser);

            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
            when(loanRequestRepository.findById(1L)).thenReturn(Optional.of(loanRequest));

            assertThrows(LoanRequestAuthorizationException.class, () -> loanRequestService.cancelRequest(1L));
        }

        @Test
        @DisplayName("Should throw InvalidLoanRequestStatusException when status is not pending")
        void cancelRequestInvalidStatus() {
            LoanRequest loanRequest = new LoanRequest();
            loanRequest.setUser(user);
            loanRequest.setStatus(LoanStatus.APPROVED);

            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
            when(loanRequestRepository.findById(1L)).thenReturn(Optional.of(loanRequest));

            assertThrows(InvalidLoanRequestStatusException.class, () -> loanRequestService.cancelRequest(1L));
        }
    }
}
