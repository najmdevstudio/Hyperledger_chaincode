package com.bankchaincode.exceptions;

public class DuplicateResourceException extends CustomException {
    public DuplicateResourceException(String resourceType, String resourceId) {
        super(String.format("%s already exists with ID: %s",resourceType,resourceId));
    }
}
