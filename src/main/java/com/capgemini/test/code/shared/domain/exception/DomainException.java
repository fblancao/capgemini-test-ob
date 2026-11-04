package com.capgemini.test.code.shared.domain.exception;

/**
 * Base class for business errors. Subclasses express the error category, so the delivery layer can translate
 * them to a transport (HTTP, messaging...) without knowing every bounded context.
 */
public abstract class DomainException extends RuntimeException {

  protected DomainException(String message) {
    super(message);
  }

  protected DomainException(String message, Throwable cause) {
    super(message, cause);
  }
}

