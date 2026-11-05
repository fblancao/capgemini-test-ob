package com.capgemini.test.code;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.capgemini.test.code.support.ExternalServicesStub;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * "Local env": runs the real application (port 8080) with development-time services, so neither docker-compose nor
 * the mock-server image is needed:
 * <ul>
 *   <li>PostgreSQL 17 in a throwaway Testcontainers container, wired through {@link ServiceConnection}
 *       (fresh database with the Flyway seed on every start).</li>
 *   <li>The external APIs (check-dni, email, sms) served by the in-process WireMock stub, which mirrors
 *       {@code docker/mockserver-config/init.json}. It listens where docker-compose publishes mock-server,
 *       {@code localhost:1080} (override with the {@code LOCAL_MOCKSERVER_PORT} environment variable), so the
 *       Postman collection works unchanged. Bound to the loopback interface only.</li>
 * </ul>
 * Run with {@code mvn spring-boot:test-run} or the IntelliJ configuration "App - Local env (Testcontainers)".
 * Requires a running container engine (Docker or {@code podman machine start}) and a free port 1080 (stop
 * docker-compose's mock-server first).
 */
public class LocalCodeApplication {

  static final int DEFAULT_MOCKSERVER_PORT = 1080;

  public static void main(String[] args) {
    WireMockServer externalServices = ExternalServicesStub.start(
        options().bindAddress("127.0.0.1").port(mockServerPort()));
    String[] withStubs = Stream.concat(
            Stream.of("--external.service.url=" + externalServices.baseUrl()), Arrays.stream(args))
        .toArray(String[]::new);
    try {
      SpringApplication.from(CodeApplication::main).with(LocalDevServices.class).run(withStubs);
    } catch (RuntimeException startupFailure) {
      // WireMock threads are non-daemon: without this the JVM would hang (holding the port) after a failed start
      externalServices.stop();
      throw startupFailure;
    }
  }

  private static int mockServerPort() {
    String port = System.getenv("LOCAL_MOCKSERVER_PORT");
    return port == null || port.isBlank() ? DEFAULT_MOCKSERVER_PORT : Integer.parseInt(port.trim());
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class LocalDevServices {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
      // Not PostgresContainerSupport.POSTGRES_IMAGE: touching that class would also start the tests' singleton container
      return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"));
    }
  }
}


