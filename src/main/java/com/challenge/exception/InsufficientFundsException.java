package com.challenge.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Long walletId) {
        super("Insufficient funds in wallet " + walletId);
    }
}
