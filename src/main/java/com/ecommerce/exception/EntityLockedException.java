package com.ecommerce.exception;

public class EntityLockedException extends RuntimeException {
    public EntityLockedException(String message) {
        super(message);
    }
} 