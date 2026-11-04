package com.capgemini.test.code.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Full application (real HTTP server) against a real PostgreSQL and stubbed external APIs. Every subclass shares
 * the same Spring context (context caching).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ApplicationTestSupport extends PostgresContainerSupport {

  @DynamicPropertySource
  static void externalServices(DynamicPropertyRegistry registry) {
    registry.add("external.service.url", ExternalServicesStub::baseUrl);
  }
}

