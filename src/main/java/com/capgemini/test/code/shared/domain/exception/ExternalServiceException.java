package com.capgemini.test.code.shared.domain.exception;

/**
 * An external dependency could not be reached or answered unexpectedly.
 */
public class ExternalServiceException extends DomainException {

  public ExternalServiceException(String service, Throwable cause) {
    super("external service unavailable: " + service, cause);
  }
}

