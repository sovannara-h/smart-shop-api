package com.ecommerce.exception;

public class ProductVariantException extends RuntimeException {
  public ProductVariantException(String message) {
    super(message);
  }

  public ProductVariantException(String message, Throwable cause) {
    super(message, cause);
  }
}
