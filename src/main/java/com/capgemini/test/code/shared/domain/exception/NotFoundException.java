package com.capgemini.test.code.shared.domain.exception;

/**
 * The requested resource does not exist.
 */
public class NotFoundException extends DomainException {

  public NotFoundException(String message) {
    super(message);
  }
}

