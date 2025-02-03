package com.fintech.loansystem.controller;

import com.fintech.loansystem.dto.LoanRequestDto;
import com.fintech.loansystem.dto.LoanResponseDto;
import com.fintech.loansystem.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loan Management", description = "APIs for managing loans")
public class LoanController {

    private final LoanService loanService;

    @Operation(summary = "Create a new loan")
    @PostMapping
    public ResponseEntity<LoanResponseDto> createLoan(@Valid @RequestBody LoanRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.createLoan(requestDto));
    }

    @Operation(summary = "Get loan by ID")
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDto> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @Operation(summary = "Get all loans")
    @GetMapping
    public ResponseEntity<List<LoanResponseDto>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @Operation(summary = "Update a loan")
    @PutMapping("/{id}")
    public ResponseEntity<LoanResponseDto> updateLoan(@PathVariable Long id,
                                                      @Valid @RequestBody LoanRequestDto requestDto) {
        return ResponseEntity.ok(loanService.updateLoan(id, requestDto));
    }

    @Operation(summary = "Delete a loan")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }
}
