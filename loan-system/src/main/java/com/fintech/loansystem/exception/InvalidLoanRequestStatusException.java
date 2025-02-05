package com.fintech.loansystem.exception;

public class InvalidLoanRequestStatusException extends RuntimeException {
    public InvalidLoanRequestStatusException(String message) {
        super(message);
    }
}

