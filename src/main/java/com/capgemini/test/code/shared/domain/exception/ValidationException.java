package com.capgemini.test.code.shared.domain.exception;

/**
 * A business rule rejected the request. Message format required by the specification:
 * {@code error validation <field>}.
 */
public class ValidationException extends DomainException {

  private static final String MESSAGE_PREFIX = "error validation ";

  private final String field;

  public ValidationException(String field) {
    super(MESSAGE_PREFIX + field);
    this.field = field;
  }

  public String getField() {
    return field;
  }
}

