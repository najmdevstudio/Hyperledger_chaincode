package com.bankchaincode.exceptions;

public class CustomerNotFoundException extends CustomException {
    public CustomerNotFoundException(String customerId) {
        super(String.format("Customer with ID %s not found.", customerId));
    }
}
