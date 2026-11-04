package com.capgemini.test.code.shared.infrastructure.config;

import com.capgemini.test.code.clients.DniClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Enables the declarative HTTP clients of the {@code clients} package (transport, timeouts and logging are
 * configured under {@code spring.cloud.openfeign} in application.yml).
 */
@Configuration(proxyBeanMethods = false)
@EnableFeignClients(basePackageClasses = DniClient.class)
public class FeignConfiguration {
}

