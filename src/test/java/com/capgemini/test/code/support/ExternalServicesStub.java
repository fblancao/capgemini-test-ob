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
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * In-process stub of the external APIs. The default stubs mirror {@code docker/mockserver-config/init.json}:
 * <ul>
 *   <li>PATCH /check-dni: 409 for dni "99999999w", 200 otherwise.</li>
 *   <li>POST /email and POST /sms: 200.</li>
 * </ul>
 * Tests share one server on a random port ({@link #server()}); {@link #start(WireMockConfiguration)} creates extra
 * servers, e.g. the "Local env" one on the mock-server port 1080.
 */
public final class ExternalServicesStub {

  public static final String INVALID_DNI = "99999999w";

  /** Lazy holder: the shared test server only starts when a test uses it. */
  private static final class Shared {
    private static final WireMockServer SERVER = start(options().dynamicPort());
  }

  private ExternalServicesStub() {
  }

  /** Starts a new stub server with the default contract; it is stopped when the JVM exits. */
  public static WireMockServer start(WireMockConfiguration options) {
    WireMockServer server = new WireMockServer(options);
    server.start();
    Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    stubDefaults(server);
    return server;
  }

  public static WireMockServer server() {
    return Shared.SERVER;
  }

  public static String baseUrl() {
    return server().baseUrl();
  }

  /** Restores the default stubs and forgets the received requests. */
  public static void reset() {
    server().resetAll();
    stubDefaults(server());
  }

  public static void dniServiceFailsWith(int status) {
    server().stubFor(patch(urlEqualTo("/check-dni")).atPriority(0).willReturn(aResponse().withStatus(status)));
  }

  public static void notificationServiceFailsWith(int status) {
    server().stubFor(post(urlEqualTo("/email")).atPriority(0).willReturn(aResponse().withStatus(status)));
    server().stubFor(post(urlEqualTo("/sms")).atPriority(0).willReturn(aResponse().withStatus(status)));
  }

  private static void stubDefaults(WireMockServer server) {
    server.stubFor(patch(urlEqualTo("/check-dni"))
        .atPriority(1)
        .withRequestBody(matchingJsonPath("$.dni", equalTo(INVALID_DNI)))
        .willReturn(json(409, "{\"message\": \"Invalid DNI\"}")));
    server.stubFor(patch(urlEqualTo("/check-dni"))
        .atPriority(5)
        .willReturn(json(200, "{\"message\": \"Valid DNI\"}")));
    server.stubFor(post(urlEqualTo("/email")).willReturn(json(200, "{\"message\": \"valid email\"}")));
    server.stubFor(post(urlEqualTo("/sms")).willReturn(json(200, "{\"message\": \"valid sms\"}")));
  }

  private static ResponseDefinitionBuilder json(int status, String body) {
    return aResponse().withStatus(status).withHeader("Content-Type", "application/json").withBody(body);
  }
}

