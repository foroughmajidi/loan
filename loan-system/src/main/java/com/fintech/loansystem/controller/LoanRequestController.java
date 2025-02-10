package com.fintech.loansystem.controller;

import com.fintech.loansystem.dto.LoanReqResponseDto;
import com.fintech.loansystem.dto.LoanRequestDto;
import com.fintech.loansystem.service.LoanRequestService;
import com.fintech.loansystem.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan-requests")
@RequiredArgsConstructor
@Tag(name = "Loan Requests", description = "APIs for requesting loans")
public class LoanRequestController {
    private final LoanRequestService loanRequestService;
    private final LoanService loanService;


    @Operation(summary = "Request a loan")
    @PostMapping("/requestLoan")
    public ResponseEntity<LoanReqResponseDto> requestLoan(@RequestBody @Valid LoanRequestDto loanRequestDto) {
        LoanReqResponseDto responseDto = loanRequestService.requestLoan(loanRequestDto);
        return ResponseEntity.ok().body(responseDto);
    }

    @Operation(summary = "cancel a loan")
    @PutMapping("cancelLoanRequest/{id}")
    public ResponseEntity<LoanReqResponseDto> CancelLoanRequest(@PathVariable Long id) {
        LoanReqResponseDto responseDto = loanRequestService.cancelRequest(id);
        return ResponseEntity.ok().body(responseDto);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/accept/{loanRequestId}")
    public ResponseEntity<LoanReqResponseDto> acceptLoanRequest(@PathVariable Long loanRequestId) {
        LoanReqResponseDto loanResponse = loanRequestService.acceptLoanRequest(loanRequestId);
        return new ResponseEntity<>(loanResponse, HttpStatus.OK);
    }

    @PutMapping("/reject/{loanRequestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanReqResponseDto> rejectLoanRequest(@PathVariable Long loanRequestId) {
        LoanReqResponseDto loanResponse = loanRequestService.rejectLoanRequest(loanRequestId);
        return new ResponseEntity<>(loanResponse, HttpStatus.OK);
    }
}
