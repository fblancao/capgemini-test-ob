package com.capgemini.test.code.shared.infrastructure.rest;

import com.capgemini.test.code.shared.domain.exception.ExternalServiceException;
import com.capgemini.test.code.shared.domain.exception.NotFoundException;
import com.capgemini.test.code.shared.domain.exception.ValidationException;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Translates error categories into HTTP responses for every bounded context.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /** The specification requires 409 Conflict for every validation error. */
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
    return build(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
    return build(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(ExternalServiceException.class)
  public ResponseEntity<ErrorResponse> handleExternalService(ExternalServiceException e) {
    log.error(e.getMessage(), e);
    return build(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
  }

  @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
    return build(HttpStatus.BAD_REQUEST, "malformed request");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
    // Standard Spring MVC errors (unknown path, method not allowed, unsupported media type...) keep their status
    if (e instanceof org.springframework.web.ErrorResponse springError) {
      HttpStatus status = HttpStatus.valueOf(springError.getStatusCode().value());
      return build(status, status.getReasonPhrase().toLowerCase(Locale.ROOT));
    }
    log.error("Unexpected error", e);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal error");
  }

  private static ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
  }
}



