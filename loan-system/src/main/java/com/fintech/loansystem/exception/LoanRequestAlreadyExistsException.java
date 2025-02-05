package com.fintech.loansystem.exception;

public class LoanRequestAlreadyExistsException extends RuntimeException {
    public LoanRequestAlreadyExistsException(String message) {
            super(message);
        }
}
