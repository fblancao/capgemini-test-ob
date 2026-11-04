package com.capgemini.test.code.user.infrastructure.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.capgemini.test.code.shared.domain.exception.ExternalServiceException;
import com.capgemini.test.code.user.application.port.in.CreateUserCommand;
import com.capgemini.test.code.user.application.port.in.CreateUserUseCase;
import com.capgemini.test.code.user.application.port.in.GetUserUseCase;
import com.capgemini.test.code.user.domain.exception.InvalidDniException;
import com.capgemini.test.code.user.domain.exception.RoomNotFoundException;
import com.capgemini.test.code.user.domain.exception.UserAlreadyExistsException;
import com.capgemini.test.code.user.domain.exception.UserNotFoundException;
import com.capgemini.test.code.user.domain.exception.UserValidationException;
import com.capgemini.test.code.user.domain.model.Role;
import com.capgemini.test.code.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

  private static final String USERS = "/rooms/1/users";
  private static final String BODY = """
      {
        "name": "pablo",
        "email": "email@email.com",
        "phone": "677998899",
        "rol": "admin",
        "dni": "23454234W"
      }
      """;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CreateUserUseCase createUserUseCase;
  @MockBean
  private GetUserUseCase getUserUseCase;

  @Test
  void createReturns201WithTheIdAndLocation() throws Exception {
    when(createUserUseCase.create(any())).thenReturn(10L);

    mockMvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", endsWith("/rooms/1/users/10")))
        .andExpect(jsonPath("$.id").value(10));

    ArgumentCaptor<CreateUserCommand> captor = ArgumentCaptor.forClass(CreateUserCommand.class);
    verify(createUserUseCase).create(captor.capture());
    assertThat(captor.getValue())
        .isEqualTo(new CreateUserCommand(1L, "pablo", "email@email.com", "677998899", "admin", "23454234W"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"userName", "email", "rol", "phone", "dni"})
  void validationErrorsReturn409WithTheSpecifiedBody(String field) throws Exception {
    when(createUserUseCase.create(any())).thenThrow(new UserValidationException(field));

    mockMvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value(409))
        .andExpect(jsonPath("$.message").value("error validation " + field));
  }

  @Test
  void existingEmailReturns409() throws Exception {
    when(createUserUseCase.create(any())).thenThrow(new UserAlreadyExistsException("email"));

    mockMvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("error validation email"));
  }

  @Test
  void invalidDniReturns409() throws Exception {
    when(createUserUseCase.create(any())).thenThrow(new InvalidDniException());

    mockMvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value(409))
        .andExpect(jsonPath("$.message").value("error validation dni"));
  }

  @Test
  void unknownRoomReturns404() throws Exception {
    when(createUserUseCase.create(any())).thenThrow(new RoomNotFoundException(1L));

    mockMvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404));
  }

  @Test
  void unavailableExternalServiceReturns503() throws Exception {
    when(createUserUseCase.create(any()))
        .thenThrow(new ExternalServiceException("check-dni", new RuntimeException("timeout")));

    mockMvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.code").value(503))
        .andExpect(jsonPath("$.message").value("external service unavailable: check-dni"));
  }

  @Test
  void unexpectedErrorsReturn500WithoutInternalDetails() throws Exception {
    when(createUserUseCase.create(any())).thenThrow(new IllegalStateException("db password is wrong"));

    mockMvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value(500))
        .andExpect(jsonPath("$.message").value("internal error"));
  }

  @Test
  void malformedJsonReturns400() throws Exception {
    mockMvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content("{ not json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(400));
    verifyNoInteractions(createUserUseCase);
  }

  @Test
  void unsupportedMediaTypeKeepsItsStatus() throws Exception {
    mockMvc.perform(post(USERS).contentType(MediaType.TEXT_PLAIN).content(BODY))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.code").value(415));
  }

  @Test
  void getReturnsTheUser() throws Exception {
    when(getUserUseCase.get(1L, 10L)).thenReturn(
        User.restore(10L, "pablo", "email@email.com", "677998899", Role.SUPERADMIN, "23454234W", 1L));

    mockMvc.perform(get(USERS + "/10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.name").value("pablo"))
        .andExpect(jsonPath("$.email").value("email@email.com"))
        .andExpect(jsonPath("$.phone").value("677998899"))
        .andExpect(jsonPath("$.rol").value("superadmin"))
        .andExpect(jsonPath("$.dni").value("23454234W"))
        .andExpect(jsonPath("$.roomId").value(1));
  }

  @Test
  void getUnknownUserReturns404() throws Exception {
    when(getUserUseCase.get(1L, 99L)).thenThrow(new UserNotFoundException(99L, 1L));

    mockMvc.perform(get(USERS + "/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("user 99 not found in room 1"));
  }

  @Test
  void getWithNonNumericIdReturns400() throws Exception {
    mockMvc.perform(get(USERS + "/abc"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(400));
  }
}

