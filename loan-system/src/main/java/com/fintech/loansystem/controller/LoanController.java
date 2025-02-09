package com.fintech.loansystem.controller;

import com.fintech.loansystem.dto.LoanDto;
import com.fintech.loansystem.dto.LoanResponseDto;
import com.fintech.loansystem.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loan Management", description = "APIs for managing loans")
public class LoanController {

    private final LoanService loanService;

    @Operation(summary = "Create a new loan")
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponseDto> createLoan(@RequestBody @Valid LoanDto loadDto) {
        return ResponseEntity.status(HttpStatus.OK).body(loanService.createLoan(loadDto));
    }

    @Operation(summary = "Get loan by ID")
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDto> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @Operation(summary = "Get all loans")
    @GetMapping("/findLoans")
    public ResponseEntity<List<LoanResponseDto>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @Operation(summary = "Update a loan")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateLoan/{id}")
    public ResponseEntity<LoanResponseDto> updateLoan(@PathVariable Long id, @RequestBody @Valid LoanDto loanDto) {
        return ResponseEntity.ok(loanService.updateLoan(id, loanDto));
    }

    @Operation(summary = "Delete a loan")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteLoan/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/accept/{loanId}")
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

    @Operation(summary = "Get all available loan plans")
    @GetMapping("/loanNames")
    public ResponseEntity<List<String>> getAllLoanPlans() {
        return ResponseEntity.ok(loanService.getAllLoanName());
    }
}
