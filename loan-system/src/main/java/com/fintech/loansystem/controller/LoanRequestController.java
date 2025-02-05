package com.fintech.loansystem.controller;

import com.fintech.loansystem.dto.LoanReqResponseDto;
import com.fintech.loansystem.dto.LoanRequestDto;
import com.fintech.loansystem.dto.LoanResponseDto;
import com.fintech.loansystem.service.LoanRequestService;
import com.fintech.loansystem.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-requests")
@RequiredArgsConstructor
@Tag(name = "Loan Requests", description = "APIs for requesting loans")
public class LoanRequestController {
    private final LoanRequestService loanRequestService;
    private final LoanService loanService;


    @Operation(summary = "Get all available loan plans")
    @GetMapping("/loanNames")
    public ResponseEntity<List<String>> getAllLoanPlans() {
        return ResponseEntity.ok(loanService.getAllLoanName());
    }

    @Operation(summary = "Request a loan")
    @PostMapping
    public ResponseEntity<LoanReqResponseDto> requestLoan(@RequestBody LoanRequestDto loanRequestDto) {
        LoanReqResponseDto responseDto = loanRequestService.requestLoan(loanRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponseDto> acceptLoan(@PathVariable Long loanId) {
        LoanResponseDto loanResponse = loanService.acceptLoan(loanId);
        return new ResponseEntity<>(loanResponse, HttpStatus.OK);
    }

    @PutMapping("/reject/{loanId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponseDto> rejectLoan(@PathVariable Long loanId) {
        LoanResponseDto loanResponse = loanService.rejectLoan(loanId);
        return new ResponseEntity<>(loanResponse, HttpStatus.OK);
    }

}
