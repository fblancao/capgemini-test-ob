package com.capgemini.test.code.user.application.port.out;

import com.capgemini.test.code.user.domain.model.NotificationChannel;
import com.capgemini.test.code.user.domain.model.User;

/**
 * Strategy that delivers a message to a user through a given {@link NotificationChannel}.
 */
public interface NotificationSender {

  NotificationChannel channel();

  void send(User user, String message);
}

