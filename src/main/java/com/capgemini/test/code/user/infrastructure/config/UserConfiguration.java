package com.capgemini.test.code.user.infrastructure.config;

import com.capgemini.test.code.user.application.port.in.CreateUserUseCase;
import com.capgemini.test.code.user.application.port.in.GetUserUseCase;
import com.capgemini.test.code.user.application.port.out.DniValidator;
import com.capgemini.test.code.user.application.port.out.NotificationSender;
import com.capgemini.test.code.user.application.port.out.RoomRepository;
import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.application.service.CreateUserService;
import com.capgemini.test.code.user.application.service.GetUserService;
import com.capgemini.test.code.user.application.service.UserNotificationService;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-agnostic application layer of the "user" bounded context.
 */
@Configuration(proxyBeanMethods = false)
public class UserConfiguration {

  @Bean
  UserNotificationService userNotificationService(List<NotificationSender> notificationSenders) {
    return new UserNotificationService(notificationSenders);
  }

  @Bean
  CreateUserUseCase createUserUseCase(UserRepository userRepository, RoomRepository roomRepository,
      DniValidator dniValidator, UserNotificationService userNotificationService) {
    return new CreateUserService(userRepository, roomRepository, dniValidator, userNotificationService);
  }

  @Bean
  GetUserUseCase getUserUseCase(UserRepository userRepository) {
    return new GetUserService(userRepository);
  }
}

