package com.capgemini.test.code;

import static org.assertj.core.api.Assertions.assertThat;

import com.capgemini.test.code.support.ApplicationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Smoke test: the whole application starts against real infrastructure.
 */
class CodeApplicationIT extends ApplicationTestSupport {

  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void contextLoadsAndIsHealthy() {
    ResponseEntity<String> health = restTemplate.getForEntity("/actuator/health", String.class);

    assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(health.getBody()).contains("\"status\":\"UP\"");
  }

  @Test
  void flywayMigrationsWereApplied() {
    Integer applied = jdbcTemplate.queryForObject(
        "SELECT count(*) FROM flyway_schema_history WHERE success", Integer.class);
    String idType = jdbcTemplate.queryForObject(
        "SELECT data_type FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'id'",
        String.class);

    assertThat(applied).isGreaterThanOrEqualTo(2);
    assertThat(idType).isEqualTo("bigint");
  }

  @Test
  void apiDocumentationIsPublished() {
    ResponseEntity<String> docs = restTemplate.getForEntity("/v3/api-docs", String.class);

    assertThat(docs.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(docs.getBody()).contains("/rooms/{roomId}/users");
  }
}

