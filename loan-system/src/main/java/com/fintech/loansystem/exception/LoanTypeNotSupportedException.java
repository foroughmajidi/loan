package com.fintech.loansystem.exception;

public class LoanTypeNotSupportedException extends RuntimeException {
    public LoanTypeNotSupportedException(String message) {
        super(message);
    }
}
