package com.capgemini.test.code.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Real PostgreSQL (same major version as docker/docker-compose.yml) shared by every integration test of the JVM
 * (singleton container pattern). Spring Boot wires the datasource through {@link ServiceConnection}.
 */
public abstract class PostgresContainerSupport {

  public static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:17");

  @ServiceConnection
  protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRES_IMAGE);

  static {
    POSTGRES.start();
  }
}

