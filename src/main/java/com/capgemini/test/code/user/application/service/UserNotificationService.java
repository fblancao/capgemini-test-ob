package com.capgemini.test.code.user.application.service;

import com.capgemini.test.code.user.application.port.out.NotificationSender;
import com.capgemini.test.code.user.domain.model.NotificationChannel;
import com.capgemini.test.code.user.domain.model.User;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notifies users through the channel associated with their role (Strategy pattern: one
 * {@link NotificationSender} per {@link NotificationChannel}).
 *
 * <p>A notification failure never undoes the user creation: the user is already stored, so the failure is
 * logged. A transactional outbox would be the next step to guarantee delivery.
 */
public class UserNotificationService {

  public static final String USER_SAVED_MESSAGE = "usuario guardado";

  private static final Logger log = LoggerFactory.getLogger(UserNotificationService.class);

  private final Map<NotificationChannel, NotificationSender> senders = new EnumMap<>(NotificationChannel.class);

  public UserNotificationService(Collection<NotificationSender> senders) {
    senders.forEach(sender -> {
      if (this.senders.putIfAbsent(sender.channel(), sender) != null) {
        throw new IllegalStateException("Duplicated notification sender for channel " + sender.channel());
      }
    });
    for (NotificationChannel channel : NotificationChannel.values()) {
      if (!this.senders.containsKey(channel)) {
        throw new IllegalStateException("No notification sender registered for channel " + channel);
      }
    }
  }

  public void notifyUserSaved(User user) {
    NotificationChannel channel = user.getRole().getNotificationChannel();
    try {
      senders.get(channel).send(user, USER_SAVED_MESSAGE);
    } catch (RuntimeException e) {
      log.warn("Could not notify {} through {}", user, channel, e);
    }
  }
}

