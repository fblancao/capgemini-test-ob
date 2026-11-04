package com.capgemini.test.code.user.domain.exception;

/**
 * A user with the same unique attribute (e-mail by default) is already stored.
 */
public class UserAlreadyExistsException extends UserValidationException {

  public UserAlreadyExistsException(String field) {
    super(field);
  }
}

