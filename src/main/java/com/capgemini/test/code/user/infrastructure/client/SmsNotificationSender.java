package com.capgemini.test.code.user.infrastructure.client;

import com.capgemini.test.code.clients.NotificationClient;
import com.capgemini.test.code.clients.SmsRequest;
import com.capgemini.test.code.shared.domain.exception.ExternalServiceException;
import com.capgemini.test.code.user.application.port.out.NotificationSender;
import com.capgemini.test.code.user.domain.model.NotificationChannel;
import com.capgemini.test.code.user.domain.model.User;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
class SmsNotificationSender implements NotificationSender {

  static final String SERVICE_NAME = "sms";

  private final NotificationClient notificationClient;

  SmsNotificationSender(NotificationClient notificationClient) {
    this.notificationClient = notificationClient;
  }

  @Override
  public NotificationChannel channel() {
    return NotificationChannel.SMS;
  }

  @Override
  public void send(User user, String message) {
    try {
      notificationClient.sendSms(new SmsRequest(user.getPhone(), message));
    } catch (FeignException e) {
      throw new ExternalServiceException(SERVICE_NAME, e);
    }
  }
}

