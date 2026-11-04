package com.capgemini.test.code.user.application.port.out;

/**
 * Validates a DNI against an external authority.
 */
public interface DniValidator {

  /**
   * @return {@code true} if the DNI is accepted, {@code false} if it is rejected
   * @throws com.capgemini.test.code.shared.domain.exception.ExternalServiceException if the validation could
   *     not be performed
   */
  boolean isValid(String dni);
}

