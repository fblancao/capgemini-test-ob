package com.capgemini.test.code.acceptance;

import com.capgemini.test.code.support.ApplicationTestSupport;
import io.cucumber.spring.CucumberContextConfiguration;

/**
 * Acceptance tests exercise the deployed application through HTTP: real PostgreSQL, stubbed external APIs.
 */
@CucumberContextConfiguration
public class CucumberSpringConfiguration extends ApplicationTestSupport {
}

