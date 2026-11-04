package com.capgemini.test.code.user.application.service;

import com.capgemini.test.code.user.application.port.in.CreateUserCommand;
import com.capgemini.test.code.user.application.port.in.CreateUserUseCase;
import com.capgemini.test.code.user.application.port.out.DniValidator;
import com.capgemini.test.code.user.application.port.out.RoomRepository;
import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.domain.exception.InvalidDniException;
import com.capgemini.test.code.user.domain.exception.RoomNotFoundException;
import com.capgemini.test.code.user.domain.exception.UserAlreadyExistsException;
import com.capgemini.test.code.user.domain.exception.UserValidationException;
import com.capgemini.test.code.user.domain.model.User;

/**
 * Validation pipeline (cheapest checks first):
 * <ol>
 *   <li>Field rules (name, e-mail, role...) - in memory.</li>
 *   <li>Room existence and e-mail uniqueness - database.</li>
 *   <li>DNI - external API.</li>
 * </ol>
 * Then stores the user and notifies it according to its role.
 */
public class CreateUserService implements CreateUserUseCase {

  private final UserRepository userRepository;
  private final RoomRepository roomRepository;
  private final DniValidator dniValidator;
  private final UserNotificationService notificationService;

  public CreateUserService(UserRepository userRepository, RoomRepository roomRepository,
      DniValidator dniValidator, UserNotificationService notificationService) {
    this.userRepository = userRepository;
    this.roomRepository = roomRepository;
    this.dniValidator = dniValidator;
    this.notificationService = notificationService;
  }

  @Override
  public Long create(CreateUserCommand command) {
    User user = User.create(command.name(), command.email(), command.phone(), command.role(), command.dni(),
        command.roomId());

    if (!roomRepository.existsById(user.getRoomId())) {
      throw new RoomNotFoundException(user.getRoomId());
    }
    if (userRepository.existsByEmail(user.getEmail())) {
      throw new UserAlreadyExistsException(UserValidationException.EMAIL);
    }
    if (!dniValidator.isValid(user.getDni())) {
      throw new InvalidDniException();
    }

    User saved = userRepository.save(user);
    notificationService.notifyUserSaved(saved);
    return saved.getId();
  }
}

