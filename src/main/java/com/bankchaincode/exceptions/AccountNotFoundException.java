package com.bankchaincode.exceptions;

public class AccountNotFoundException extends CustomException {
    public AccountNotFoundException(String accountId) {
        super(String.format("Account with id %s not found.", accountId));
    }
}
