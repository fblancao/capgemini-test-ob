package com.capgemini.test.code.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.capgemini.test.code.support.ExternalServicesStub;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Real Feign clients (OkHttp transport, Jackson encoding) against an HTTP stub of the mock-server contract.
 */
@SpringBootTest(classes = FeignClientsIT.ClientsConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FeignClientsIT {

  @Configuration(proxyBeanMethods = false)
  @EnableFeignClients(clients = {DniClient.class, NotificationClient.class})
  @ImportAutoConfiguration({JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
      FeignAutoConfiguration.class})
  static class ClientsConfiguration {
  }

  @DynamicPropertySource
  static void externalServices(DynamicPropertyRegistry registry) {
    registry.add("external.service.url", ExternalServicesStub::baseUrl);
  }

  @Autowired
  private DniClient dniClient;
  @Autowired
  private NotificationClient notificationClient;

  @BeforeEach
  void resetStubs() {
    ExternalServicesStub.reset();
  }

  @Test
  void checkDniSendsAPatchWithTheDniAndReadsTheResponse() {
    ResponseEntity<CheckDniResponse> response = dniClient.check(new CheckDniRequest("23454234W"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Valid DNI");
    ExternalServicesStub.server().verify(1, patchRequestedFor(urlEqualTo("/check-dni"))
        .withRequestBody(equalToJson("{\"dni\": \"23454234W\"}")));
  }

  @Test
  void checkDniThrowsConflictForTheRejectedDni() {
    assertThatThrownBy(() -> dniClient.check(new CheckDniRequest(ExternalServicesStub.INVALID_DNI)))
        .isInstanceOf(FeignException.Conflict.class);
  }

  @Test
  void sendEmailPostsTheEmailAndMessage() {
    ResponseEntity<NotificationResponse> response =
        notificationClient.sendEmail(new EmailRequest("email@email.com", "usuario guardado"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    ExternalServicesStub.server().verify(1, postRequestedFor(urlEqualTo("/email"))
        .withRequestBody(equalToJson("{\"email\": \"email@email.com\", \"message\": \"usuario guardado\"}")));
  }

  @Test
  void sendSmsPostsThePhoneAndMessage() {
    ResponseEntity<NotificationResponse> response =
        notificationClient.sendSms(new SmsRequest("677998899", "usuario guardado"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    ExternalServicesStub.server().verify(1, postRequestedFor(urlEqualTo("/sms"))
        .withRequestBody(equalToJson("{\"phone\": \"677998899\", \"message\": \"usuario guardado\"}")));
  }

  @Test
  void serverErrorsAreReportedAsFeignExceptions() {
    ExternalServicesStub.notificationServiceFailsWith(500);

    assertThatThrownBy(() -> notificationClient.sendSms(new SmsRequest("677998899", "usuario guardado")))
        .isInstanceOf(FeignException.InternalServerError.class);
  }
}

