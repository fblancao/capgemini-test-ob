package com.capgemini.test.code.user.infrastructure.client;

import com.capgemini.test.code.clients.CheckDniRequest;
import com.capgemini.test.code.clients.DniClient;
import com.capgemini.test.code.shared.domain.exception.ExternalServiceException;
import com.capgemini.test.code.user.application.port.out.DniValidator;
import feign.FeignException;
import org.springframework.stereotype.Component;

/**
 * Adapts the external check-dni API: HTTP 2xx means valid, HTTP 409 means invalid; anything else means the
 * validation could not be performed.
 */
@Component
class FeignDniValidator implements DniValidator {

  static final String SERVICE_NAME = "check-dni";

  private final DniClient dniClient;

  FeignDniValidator(DniClient dniClient) {
    this.dniClient = dniClient;
  }

  @Override
  public boolean isValid(String dni) {
    try {
      dniClient.check(new CheckDniRequest(dni));
      return true;
    } catch (FeignException.Conflict e) {
      return false;
    } catch (FeignException e) {
      throw new ExternalServiceException(SERVICE_NAME, e);
    }
  }
}

