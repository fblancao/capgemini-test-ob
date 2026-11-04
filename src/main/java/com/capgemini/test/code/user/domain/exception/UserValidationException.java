package com.capgemini.test.code.user.domain.exception;

import com.capgemini.test.code.shared.domain.exception.ValidationException;

/**
 * A user attribute does not satisfy the business rules.
 */
public class UserValidationException extends ValidationException {

  public static final String USER_NAME = "userName";
  public static final String EMAIL = "email";
  public static final String ROLE = "rol";
  public static final String PHONE = "phone";
  public static final String DNI = "dni";

  public UserValidationException(String field) {
    super(field);
  }
}

