package com.capgemini.test.code.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.capgemini.test.code.user.domain.exception.UserValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

  private static final String NAME = "pablo";
  private static final String EMAIL = "email@email.com";
  private static final String PHONE = "677998899";
  private static final String ROLE = "admin";
  private static final String DNI = "23454234W";
  private static final Long ROOM_ID = 1L;

  @Test
  @DisplayName("creates a valid user with the example of the specification")
  void createsValidUser() {
    User user = User.create(NAME, EMAIL, PHONE, ROLE, DNI, ROOM_ID);

    assertThat(user.getId()).isNull();
    assertThat(user.getName()).isEqualTo(NAME);
    assertThat(user.getEmail()).isEqualTo(EMAIL);
    assertThat(user.getPhone()).isEqualTo(PHONE);
    assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    assertThat(user.getDni()).isEqualTo(DNI);
    assertThat(user.getRoomId()).isEqualTo(ROOM_ID);
  }

  @Test
  void normalizesInput() {
    User user = User.create("  pablo ", " Email@Email.COM ", " 677998899 ", " SuperAdmin ", " 23454234W ", ROOM_ID);

    assertThat(user.getName()).isEqualTo("pablo");
    assertThat(user.getEmail()).isEqualTo("email@email.com");
    assertThat(user.getPhone()).isEqualTo("677998899");
    assertThat(user.getRole()).isEqualTo(Role.SUPERADMIN);
    assertThat(user.getDni()).isEqualTo("23454234W");
  }

  @Test
  void dniCaseIsPreservedBecauseTheExternalValidatorIsCaseSensitive() {
    assertThat(User.create(NAME, EMAIL, PHONE, ROLE, "99999999w", ROOM_ID).getDni()).isEqualTo("99999999w");
  }

  @ParameterizedTest
  @ValueSource(strings = {"a", "pablo", "pablos"})
  void acceptsNamesUpToSixCharacters(String name) {
    assertThat(User.create(name, EMAIL, PHONE, ROLE, DNI, ROOM_ID).getName()).isEqualTo(name);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "pablito", "francisco"})
  void rejectsInvalidNames(String name) {
    assertValidationError(() -> User.create(name, EMAIL, PHONE, ROLE, DNI, ROOM_ID), "userName");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"emailemail.com", "email@emailcom", "email@", "@email.com", "email@.com", "email@email.",
      "em ail@email.com", "email@@email.com"})
  void rejectsInvalidEmails(String email) {
    assertValidationError(() -> User.create(NAME, email, PHONE, ROLE, DNI, ROOM_ID), "email");
  }

  @Test
  void acceptsEmailUpToTheColumnLength() {
    String email = "a".repeat(140) + "@email.com"; // 150 characters
    assertThat(User.create(NAME, email, PHONE, ROLE, DNI, ROOM_ID).getEmail()).hasSize(150);
  }

  @Test
  void rejectsTooLongEmail() {
    String email = "a".repeat(141) + "@email.com"; // 151 characters
    assertValidationError(() -> User.create(NAME, email, PHONE, ROLE, DNI, ROOM_ID), "email");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"user", "administrator", "super admin"})
  void rejectsInvalidRoles(String role) {
    assertValidationError(() -> User.create(NAME, EMAIL, PHONE, role, DNI, ROOM_ID), "rol");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "1234567890123456"})
  void rejectsInvalidPhones(String phone) {
    assertValidationError(() -> User.create(NAME, EMAIL, phone, ROLE, DNI, ROOM_ID), "phone");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "1234567890123456"})
  void rejectsInvalidDnis(String dni) {
    assertValidationError(() -> User.create(NAME, EMAIL, PHONE, ROLE, dni, ROOM_ID), "dni");
  }

  @Test
  void reportsTheFirstInvalidFieldInSpecificationOrder() {
    assertValidationError(() -> User.create("pablito", "invalid", PHONE, "user", DNI, ROOM_ID), "userName");
  }

  @Test
  void requiresARoom() {
    assertThatThrownBy(() -> User.create(NAME, EMAIL, PHONE, ROLE, DNI, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void restoresPersistedUserWithoutValidatingIt() {
    User user = User.restore(7L, "Juan Pérez", "juan@example.com", "600000001", Role.ADMIN, "11111111H", 1L);

    assertThat(user.getId()).isEqualTo(7L);
    assertThat(user.getName()).isEqualTo("Juan Pérez");
  }

  @Test
  void equalityIsBasedOnIdentity() {
    User user = User.restore(1L, NAME, EMAIL, PHONE, Role.ADMIN, DNI, ROOM_ID);
    User sameId = User.restore(1L, "other", "other@email.com", "1", Role.SUPERADMIN, "2", 2L);
    User otherId = User.restore(2L, NAME, EMAIL, PHONE, Role.ADMIN, DNI, ROOM_ID);

    assertThat(user).isEqualTo(sameId).hasSameHashCodeAs(sameId).isNotEqualTo(otherId);
    assertThat(User.create(NAME, EMAIL, PHONE, ROLE, DNI, ROOM_ID))
        .isNotEqualTo(User.create(NAME, EMAIL, PHONE, ROLE, DNI, ROOM_ID));
  }

  @Test
  void toStringDoesNotExposePersonalData() {
    String text = User.create(NAME, EMAIL, PHONE, ROLE, DNI, ROOM_ID).toString();

    assertThat(text).doesNotContain(EMAIL, PHONE, DNI);
  }

  private static void assertValidationError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call,
      String field) {
    assertThatThrownBy(call)
        .isInstanceOf(UserValidationException.class)
        .hasMessage("error validation " + field)
        .extracting("field").isEqualTo(field);
  }
}

