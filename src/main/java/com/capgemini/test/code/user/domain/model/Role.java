package com.capgemini.test.code.user.domain.model;

import com.capgemini.test.code.user.domain.exception.UserValidationException;
import java.util.Arrays;
import java.util.Locale;

/**
 * Allowed user roles. Each role knows the channel used to notify it (business rule):
 * admin is notified by e-mail and superadmin by SMS.
 */
public enum Role {
  ADMIN("admin", NotificationChannel.EMAIL),
  SUPERADMIN("superadmin", NotificationChannel.SMS);

  private final String value;
  private final NotificationChannel notificationChannel;

  Role(String value, NotificationChannel notificationChannel) {
    this.value = value;
    this.notificationChannel = notificationChannel;
  }

  public String getValue() {
    return value;
  }

  public NotificationChannel getNotificationChannel() {
    return notificationChannel;
  }

  /**
   * Parses the external representation of a role (case-insensitive).
   *
   * @throws UserValidationException with field {@code rol} if the value is not an allowed role
   */
  public static Role fromValue(String value) {
    if (value == null) {
      throw new UserValidationException(UserValidationException.ROLE);
    }
    String normalized = value.trim().toLowerCase(Locale.ROOT);
    return Arrays.stream(values())
        .filter(role -> role.value.equals(normalized))
        .findFirst()
        .orElseThrow(() -> new UserValidationException(UserValidationException.ROLE));
  }
}

