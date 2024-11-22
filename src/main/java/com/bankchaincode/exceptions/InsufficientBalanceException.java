package com.bankchaincode.exceptions;

public class InsufficientBalanceException extends CustomException{
    public InsufficientBalanceException(String accountId) {
        super(String.format("Insufficient balance for account %s", accountId));
    }
}
