package com.sahrin.spas.exception;

public class AccountPendingApprovalException extends RuntimeException {
    public AccountPendingApprovalException(String message) {
        super(message);
    }
}
