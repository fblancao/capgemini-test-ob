package com.capgemini.test.code.user.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.capgemini.test.code.clients.EmailRequest;
import com.capgemini.test.code.clients.NotificationClient;
import com.capgemini.test.code.clients.SmsRequest;
import com.capgemini.test.code.shared.domain.exception.ExternalServiceException;
import com.capgemini.test.code.user.domain.model.NotificationChannel;
import com.capgemini.test.code.user.domain.model.Role;
import com.capgemini.test.code.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationSendersTest {

  private static final User USER =
      User.restore(1L, "pablo", "email@email.com", "677998899", Role.ADMIN, "23454234W", 1L);

  @Mock
  private NotificationClient notificationClient;

  private EmailNotificationSender emailSender;
  private SmsNotificationSender smsSender;

  @BeforeEach
  void setUp() {
    emailSender = new EmailNotificationSender(notificationClient);
    smsSender = new SmsNotificationSender(notificationClient);
  }

  @Test
  void eachSenderHandlesItsChannel() {
    assertThat(emailSender.channel()).isEqualTo(NotificationChannel.EMAIL);
    assertThat(smsSender.channel()).isEqualTo(NotificationChannel.SMS);
  }

  @Test
  void emailIsSentToTheUserEmail() {
    emailSender.send(USER, "usuario guardado");

    ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
    verify(notificationClient).sendEmail(captor.capture());
    assertThat(captor.getValue().getEmail()).isEqualTo("email@email.com");
    assertThat(captor.getValue().getMessage()).isEqualTo("usuario guardado");
  }

  @Test
  void smsIsSentToTheUserPhone() {
    smsSender.send(USER, "usuario guardado");

    ArgumentCaptor<SmsRequest> captor = ArgumentCaptor.forClass(SmsRequest.class);
    verify(notificationClient).sendSms(captor.capture());
    assertThat(captor.getValue().getPhone()).isEqualTo("677998899");
    assertThat(captor.getValue().getMessage()).isEqualTo("usuario guardado");
  }

  @Test
  void emailFailuresAreTranslated() {
    when(notificationClient.sendEmail(any())).thenThrow(FeignExceptions.withStatus(500));

    assertThatThrownBy(() -> emailSender.send(USER, "usuario guardado"))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessage("external service unavailable: email");
  }

  @Test
  void smsFailuresAreTranslated() {
    when(notificationClient.sendSms(any())).thenThrow(FeignExceptions.withStatus(503));

    assertThatThrownBy(() -> smsSender.send(USER, "usuario guardado"))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessage("external service unavailable: sms");
  }
}
