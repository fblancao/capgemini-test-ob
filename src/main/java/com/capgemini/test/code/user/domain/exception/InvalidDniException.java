package com.capgemini.test.code.user.domain.exception;

/**
 * The external DNI validation service rejected the DNI.
 */
public class InvalidDniException extends UserValidationException {

  public InvalidDniException() {
    super(DNI);
  }
}

