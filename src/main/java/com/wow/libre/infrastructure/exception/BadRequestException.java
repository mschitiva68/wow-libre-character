package com.wow.libre.infrastructure.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends GenericErrorException {
  public BadRequestException(String message, String transactionId) {
    super(transactionId, message, HttpStatus.BAD_REQUEST);
  }
}
