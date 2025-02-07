package com.fintech.loansystem.exception;

 public class CustomUniqueConstraintViolationException extends RuntimeException {

    public CustomUniqueConstraintViolationException(String message) {
        super(message);
    }

    public CustomUniqueConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
