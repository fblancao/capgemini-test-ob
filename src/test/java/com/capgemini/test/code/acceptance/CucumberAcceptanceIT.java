package com.capgemini.test.code.acceptance;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Runs the Gherkin acceptance criteria of {@code src/test/resources/features} (executed by failsafe).
 * HTML report: {@code target/cucumber-report.html}.
 *
 * <p>The configuration matches the defaults of {@code junit-platform.properties}, used when an IDE discovers the
 * feature files directly through the Cucumber engine (outside this suite).
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.capgemini.test.code.acceptance")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-report.html")
class CucumberAcceptanceIT {
}

