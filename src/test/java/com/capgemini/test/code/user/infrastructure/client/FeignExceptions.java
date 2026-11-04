package com.capgemini.test.code.user.infrastructure.client;

import feign.FeignException;
import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Builds real {@link FeignException} subclasses (e.g. {@code FeignException.Conflict}) for a given status.
 */
final class FeignExceptions {

  private FeignExceptions() {
  }

  static FeignException withStatus(int status) {
    Request request = Request.create(Request.HttpMethod.POST, "http://localhost/test", Map.of(), null,
        StandardCharsets.UTF_8, null);
    Response response = Response.builder()
        .status(status)
        .reason("status " + status)
        .request(request)
        .headers(Map.of())
        .build();
    return FeignException.errorStatus("Client#method()", response);
  }
}

