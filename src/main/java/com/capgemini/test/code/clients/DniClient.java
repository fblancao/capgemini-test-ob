package com.capgemini.test.code.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * External DNI validation API (mock-server).
 *
 * <p>The HTTP transport is OkHttp ({@code spring.cloud.openfeign.okhttp.enabled=true}) because the default JDK
 * client does not support PATCH. Timeouts and log level are configured in {@code application.yml} instead of a
 * {@code @Configuration} class nested in this interface, which would be component-scanned and applied to every
 * Feign client of the context (see Spring Cloud OpenFeign reference documentation).
 */
@FeignClient(name = "checkDniClient", url = "${external.service.url}")
public interface DniClient {

  @PatchMapping(value = "/check-dni")
  ResponseEntity<CheckDniResponse> check(@RequestBody CheckDniRequest request);
}

