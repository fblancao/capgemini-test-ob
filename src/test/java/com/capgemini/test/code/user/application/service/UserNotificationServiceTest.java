package com.capgemini.test.code.user.application.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.capgemini.test.code.shared.domain.exception.ExternalServiceException;
import com.capgemini.test.code.user.application.port.out.NotificationSender;
import com.capgemini.test.code.user.domain.model.NotificationChannel;
import com.capgemini.test.code.user.domain.model.Role;
import com.capgemini.test.code.user.domain.model.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserNotificationServiceTest {

  private static final User ADMIN =
      User.restore(1L, "pablo", "email@email.com", "677998899", Role.ADMIN, "23454234W", 1L);
  private static final User SUPERADMIN =
      User.restore(2L, "ana", "ana@email.com", "677998800", Role.SUPERADMIN, "23454235W", 1L);

  private NotificationSender emailSender;
  private NotificationSender smsSender;
  private UserNotificationService service;

  @BeforeEach
  void setUp() {
    emailSender = sender(NotificationChannel.EMAIL);
    smsSender = sender(NotificationChannel.SMS);
    service = new UserNotificationService(List.of(emailSender, smsSender));
  }

  @Test
  void adminIsNotifiedByEmailWithTheExpectedMessage() {
    service.notifyUserSaved(ADMIN);

    verify(emailSender).send(ADMIN, "usuario guardado");
    verify(smsSender, never()).send(any(), any());
  }

  @Test
  void superadminIsNotifiedBySmsWithTheExpectedMessage() {
    service.notifyUserSaved(SUPERADMIN);

    verify(smsSender).send(SUPERADMIN, "usuario guardado");
    verify(emailSender, never()).send(any(), any());
  }

  @Test
  void notificationFailuresDoNotPropagate() {
    doThrow(new ExternalServiceException("email", new RuntimeException())).when(emailSender).send(any(), any());

    assertThatCode(() -> service.notifyUserSaved(ADMIN)).doesNotThrowAnyException();
  }

  @Test
  void failsFastWhenAChannelHasNoSender() {
    List<NotificationSender> onlyEmail = List.of(sender(NotificationChannel.EMAIL));

    assertThatThrownBy(() -> new UserNotificationService(onlyEmail))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("SMS");
  }

  @Test
  void failsFastWhenAChannelHasSeveralSenders() {
    List<NotificationSender> duplicated =
        List.of(sender(NotificationChannel.EMAIL), sender(NotificationChannel.EMAIL), sender(NotificationChannel.SMS));

    assertThatThrownBy(() -> new UserNotificationService(duplicated))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("EMAIL");
  }

  private static NotificationSender sender(NotificationChannel channel) {
    NotificationSender sender = mock(NotificationSender.class);
    when(sender.channel()).thenReturn(channel);
    return sender;
  }
}
