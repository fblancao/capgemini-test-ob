package com.capgemini.test.code;

import com.capgemini.test.code.support.ExternalServicesStub;
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
 *       {@code docker/mockserver-config/init.json}.</li>
 * </ul>
 * Run with {@code mvn spring-boot:test-run} or the IntelliJ configuration "App - Local env (Testcontainers)".
 * Requires a running container engine (Docker or {@code podman machine start}).
 */
public class LocalCodeApplication {

  public static void main(String[] args) {
    String[] withStubs = Stream.concat(
            Stream.of("--external.service.url=" + ExternalServicesStub.baseUrl()), Arrays.stream(args))
        .toArray(String[]::new);
    SpringApplication.from(CodeApplication::main).with(LocalDevServices.class).run(withStubs);
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


