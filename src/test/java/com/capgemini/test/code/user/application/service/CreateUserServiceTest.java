package com.capgemini.test.code.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.capgemini.test.code.shared.domain.exception.ExternalServiceException;
import com.capgemini.test.code.user.application.port.in.CreateUserCommand;
import com.capgemini.test.code.user.application.port.out.DniValidator;
import com.capgemini.test.code.user.application.port.out.RoomRepository;
import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.domain.exception.InvalidDniException;
import com.capgemini.test.code.user.domain.exception.RoomNotFoundException;
import com.capgemini.test.code.user.domain.exception.UserAlreadyExistsException;
import com.capgemini.test.code.user.domain.exception.UserValidationException;
import com.capgemini.test.code.user.domain.model.Role;
import com.capgemini.test.code.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

  private static final Long ROOM_ID = 1L;
  private static final CreateUserCommand COMMAND =
      new CreateUserCommand(ROOM_ID, "pablo", "email@email.com", "677998899", "admin", "23454234W");

  @Mock
  private UserRepository userRepository;
  @Mock
  private RoomRepository roomRepository;
  @Mock
  private DniValidator dniValidator;
  @Mock
  private UserNotificationService notificationService;

  @InjectMocks
  private CreateUserService service;

  @Test
  void validatesStoresNotifiesAndReturnsTheGeneratedId() {
    when(roomRepository.existsById(ROOM_ID)).thenReturn(true);
    when(userRepository.existsByEmail("email@email.com")).thenReturn(false);
    when(dniValidator.isValid("23454234W")).thenReturn(true);
    User saved = User.restore(42L, "pablo", "email@email.com", "677998899", Role.ADMIN, "23454234W", ROOM_ID);
    when(userRepository.save(any())).thenReturn(saved);

    Long id = service.create(COMMAND);

    assertThat(id).isEqualTo(42L);
    InOrder order = inOrder(roomRepository, userRepository, dniValidator, notificationService);
    order.verify(roomRepository).existsById(ROOM_ID);
    order.verify(userRepository).existsByEmail("email@email.com");
    order.verify(dniValidator).isValid("23454234W");
    order.verify(userRepository).save(any());
    order.verify(notificationService).notifyUserSaved(saved);
  }

  @Test
  void storesTheUserInTheRequestedRoom() {
    when(roomRepository.existsById(ROOM_ID)).thenReturn(true);
    when(dniValidator.isValid(any())).thenReturn(true);
    when(userRepository.save(any())).thenAnswer(invocation -> withId(invocation.getArgument(0), 1L));

    service.create(COMMAND);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User toSave = captor.getValue();
    assertThat(toSave.getId()).isNull();
    assertThat(toSave.getRoomId()).isEqualTo(ROOM_ID);
    assertThat(toSave.getName()).isEqualTo("pablo");
    assertThat(toSave.getRole()).isEqualTo(Role.ADMIN);
  }

  @Test
  void invalidFieldsAreRejectedBeforeAnyInfrastructureCall() {
    CreateUserCommand invalid =
        new CreateUserCommand(ROOM_ID, "pablito", "email@email.com", "677998899", "admin", "23454234W");

    assertThatThrownBy(() -> service.create(invalid))
        .isInstanceOf(UserValidationException.class)
        .hasMessage("error validation userName");
    verifyNoInteractions(roomRepository, userRepository, dniValidator, notificationService);
  }

  @Test
  void rejectsUnknownRoom() {
    when(roomRepository.existsById(ROOM_ID)).thenReturn(false);

    assertThatThrownBy(() -> service.create(COMMAND)).isInstanceOf(RoomNotFoundException.class);
    verifyNoInteractions(userRepository, dniValidator, notificationService);
  }

  @Test
  void rejectsAlreadyExistingEmailWithoutCallingTheDniApi() {
    when(roomRepository.existsById(ROOM_ID)).thenReturn(true);
    when(userRepository.existsByEmail("email@email.com")).thenReturn(true);

    assertThatThrownBy(() -> service.create(COMMAND))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessage("error validation email");
    verifyNoInteractions(dniValidator, notificationService);
    verify(userRepository, never()).save(any());
  }

  @Test
  void rejectsDniRefusedByTheExternalApi() {
    when(roomRepository.existsById(ROOM_ID)).thenReturn(true);
    when(dniValidator.isValid("23454234W")).thenReturn(false);

    assertThatThrownBy(() -> service.create(COMMAND))
        .isInstanceOf(InvalidDniException.class)
        .hasMessage("error validation dni");
    verify(userRepository, never()).save(any());
    verifyNoInteractions(notificationService);
  }

  @Test
  void doesNotStoreTheUserWhenTheDniCannotBeValidated() {
    when(roomRepository.existsById(ROOM_ID)).thenReturn(true);
    when(dniValidator.isValid(any())).thenThrow(new ExternalServiceException("check-dni", new RuntimeException()));

    assertThatThrownBy(() -> service.create(COMMAND)).isInstanceOf(ExternalServiceException.class);
    verify(userRepository, never()).save(any());
    verifyNoInteractions(notificationService);
  }

  @Test
  void propagatesUniqueViolationsDetectedWhenStoring() {
    when(roomRepository.existsById(ROOM_ID)).thenReturn(true);
    when(dniValidator.isValid(any())).thenReturn(true);
    when(userRepository.save(any())).thenThrow(new UserAlreadyExistsException("dni"));

    assertThatThrownBy(() -> service.create(COMMAND)).hasMessage("error validation dni");
    verifyNoInteractions(notificationService);
  }

  private static User withId(User user, Long id) {
    return User.restore(id, user.getName(), user.getEmail(), user.getPhone(), user.getRole(), user.getDni(),
        user.getRoomId());
  }
}

