package com.capgemini.test.code.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.domain.exception.UserNotFoundException;
import com.capgemini.test.code.user.domain.model.Role;
import com.capgemini.test.code.user.domain.model.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private GetUserService service;

  @Test
  void returnsTheUserOfTheRoom() {
    User user = User.restore(5L, "pablo", "email@email.com", "677998899", Role.ADMIN, "23454234W", 1L);
    when(userRepository.findByIdAndRoomId(5L, 1L)).thenReturn(Optional.of(user));

    assertThat(service.get(1L, 5L)).isSameAs(user);
  }

  @Test
  void failsWhenTheUserIsNotInTheRoom() {
    when(userRepository.findByIdAndRoomId(5L, 2L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(2L, 5L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("user 5 not found in room 2");
  }
}

