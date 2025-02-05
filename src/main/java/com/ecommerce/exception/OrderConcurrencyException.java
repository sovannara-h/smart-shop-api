package com.ecommerce.exception;

public class OrderConcurrencyException extends RuntimeException {
    public OrderConcurrencyException(String message) {
        super(message);
    }
}
