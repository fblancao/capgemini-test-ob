package com.capgemini.test.code.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.capgemini.test.code.user.domain.exception.UserValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RoleTest {

  @ParameterizedTest
  @CsvSource({"admin,ADMIN", "ADMIN,ADMIN", " Admin ,ADMIN", "superadmin,SUPERADMIN", "SuperAdmin,SUPERADMIN"})
  void parsesAllowedRolesIgnoringCase(String value, Role expected) {
    assertThat(Role.fromValue(value)).isEqualTo(expected);
  }

  @Test
  void rejectsUnknownRole() {
    assertThatThrownBy(() -> Role.fromValue("user"))
        .isInstanceOf(UserValidationException.class)
        .hasMessage("error validation rol");
  }

  @Test
  void rejectsNullRole() {
    assertThatThrownBy(() -> Role.fromValue(null)).isInstanceOf(UserValidationException.class);
  }

  @Test
  void adminIsNotifiedByEmailAndSuperadminBySms() {
    assertThat(Role.ADMIN.getNotificationChannel()).isEqualTo(NotificationChannel.EMAIL);
    assertThat(Role.SUPERADMIN.getNotificationChannel()).isEqualTo(NotificationChannel.SMS);
  }

  @Test
  void exposesExternalValue() {
    assertThat(Role.ADMIN.getValue()).isEqualTo("admin");
    assertThat(Role.SUPERADMIN.getValue()).isEqualTo("superadmin");
  }
}

