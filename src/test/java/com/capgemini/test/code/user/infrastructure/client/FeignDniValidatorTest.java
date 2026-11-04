package com.capgemini.test.code.user.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.capgemini.test.code.clients.CheckDniRequest;
import com.capgemini.test.code.clients.CheckDniResponse;
import com.capgemini.test.code.clients.DniClient;
import com.capgemini.test.code.shared.domain.exception.ExternalServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class FeignDniValidatorTest {

  @Mock
  private DniClient dniClient;

  @InjectMocks
  private FeignDniValidator validator;

  @Test
  void dniIsValidWhenTheApiAnswersOk() {
    when(dniClient.check(any())).thenReturn(ResponseEntity.ok(new CheckDniResponse("Valid DNI")));

    assertThat(validator.isValid("23454234W")).isTrue();

    ArgumentCaptor<CheckDniRequest> captor = ArgumentCaptor.forClass(CheckDniRequest.class);
    verify(dniClient).check(captor.capture());
    assertThat(captor.getValue().getDni()).isEqualTo("23454234W");
  }

  @Test
  void dniIsInvalidWhenTheApiAnswersConflict() {
    when(dniClient.check(any())).thenThrow(FeignExceptions.withStatus(409));

    assertThat(validator.isValid("99999999w")).isFalse();
  }

  @ParameterizedTest
  @ValueSource(ints = {400, 404, 500, 503})
  void otherErrorsMeanTheValidationCouldNotBePerformed(int status) {
    when(dniClient.check(any())).thenThrow(FeignExceptions.withStatus(status));

    assertThatThrownBy(() -> validator.isValid("23454234W"))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessage("external service unavailable: check-dni");
  }
}

