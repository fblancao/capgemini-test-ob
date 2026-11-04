package com.capgemini.test.code.support;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

/**
 * In-process stub of the external APIs. The default stubs mirror {@code docker/mockserver-config/init.json}:
 * <ul>
 *   <li>PATCH /check-dni: 409 for dni "99999999w", 200 otherwise.</li>
 *   <li>POST /email and POST /sms: 200.</li>
 * </ul>
 */
public final class ExternalServicesStub {

  public static final String INVALID_DNI = "99999999w";

  private static final WireMockServer SERVER = new WireMockServer(options().dynamicPort());

  static {
    SERVER.start();
    Runtime.getRuntime().addShutdownHook(new Thread(SERVER::stop));
    stubDefaults();
  }

  private ExternalServicesStub() {
  }

  public static WireMockServer server() {
    return SERVER;
  }

  public static String baseUrl() {
    return SERVER.baseUrl();
  }

  /** Restores the default stubs and forgets the received requests. */
  public static void reset() {
    SERVER.resetAll();
    stubDefaults();
  }

  public static void dniServiceFailsWith(int status) {
    SERVER.stubFor(patch(urlEqualTo("/check-dni")).atPriority(0).willReturn(aResponse().withStatus(status)));
  }

  public static void notificationServiceFailsWith(int status) {
    SERVER.stubFor(post(urlEqualTo("/email")).atPriority(0).willReturn(aResponse().withStatus(status)));
    SERVER.stubFor(post(urlEqualTo("/sms")).atPriority(0).willReturn(aResponse().withStatus(status)));
  }

  private static void stubDefaults() {
    SERVER.stubFor(patch(urlEqualTo("/check-dni"))
        .atPriority(1)
        .withRequestBody(matchingJsonPath("$.dni", equalTo(INVALID_DNI)))
        .willReturn(json(409, "{\"message\": \"Invalid DNI\"}")));
    SERVER.stubFor(patch(urlEqualTo("/check-dni"))
        .atPriority(5)
        .willReturn(json(200, "{\"message\": \"Valid DNI\"}")));
    SERVER.stubFor(post(urlEqualTo("/email")).willReturn(json(200, "{\"message\": \"valid email\"}")));
    SERVER.stubFor(post(urlEqualTo("/sms")).willReturn(json(200, "{\"message\": \"valid sms\"}")));
  }

  private static ResponseDefinitionBuilder json(int status, String body) {
    return aResponse().withStatus(status).withHeader("Content-Type", "application/json").withBody(body);
  }
}

