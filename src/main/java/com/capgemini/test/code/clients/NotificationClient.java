package com.capgemini.test.code.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * External notification API (mock-server): e-mail and SMS channels.
 */
@FeignClient(name = "notificationClient", url = "${external.service.url}")
public interface NotificationClient {

  @PostMapping(value = "/email")
  ResponseEntity<NotificationResponse> sendEmail(@RequestBody EmailRequest request);

  @PostMapping(value = "/sms")
  ResponseEntity<NotificationResponse> sendSms(@RequestBody SmsRequest request);
}

