package com.ecommerce.exception;

public class ProductConcurrencyException extends RuntimeException {
    public ProductConcurrencyException(String message) {
        super(message);
    }
}