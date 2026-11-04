package com.capgemini.test.code.user.infrastructure.client;

import com.capgemini.test.code.clients.EmailRequest;
import com.capgemini.test.code.clients.NotificationClient;
import com.capgemini.test.code.shared.domain.exception.ExternalServiceException;
import com.capgemini.test.code.user.application.port.out.NotificationSender;
import com.capgemini.test.code.user.domain.model.NotificationChannel;
import com.capgemini.test.code.user.domain.model.User;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
class EmailNotificationSender implements NotificationSender {

  static final String SERVICE_NAME = "email";

  private final NotificationClient notificationClient;

  EmailNotificationSender(NotificationClient notificationClient) {
    this.notificationClient = notificationClient;
  }

  @Override
  public NotificationChannel channel() {
    return NotificationChannel.EMAIL;
  }

  @Override
  public void send(User user, String message) {
    try {
      notificationClient.sendEmail(new EmailRequest(user.getEmail(), message));
    } catch (FeignException e) {
      throw new ExternalServiceException(SERVICE_NAME, e);
    }
  }
}

