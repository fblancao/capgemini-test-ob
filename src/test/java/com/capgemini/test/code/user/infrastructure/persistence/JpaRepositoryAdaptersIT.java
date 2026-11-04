package com.capgemini.test.code.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.capgemini.test.code.support.PostgresContainerSupport;
import com.capgemini.test.code.user.application.port.out.RoomRepository;
import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.domain.exception.UserAlreadyExistsException;
import com.capgemini.test.code.user.domain.model.Role;
import com.capgemini.test.code.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Persistence adapters against a real PostgreSQL migrated by Flyway (each test is rolled back).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaUserRepositoryAdapter.class, JpaRoomRepositoryAdapter.class})
class JpaRepositoryAdaptersIT extends PostgresContainerSupport {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private RoomRepository roomRepository;

  @Test
  void seededRoomsExist() {
    assertThat(roomRepository.existsById(1L)).isTrue();
    assertThat(roomRepository.existsById(2L)).isTrue();
    assertThat(roomRepository.existsById(999L)).isFalse();
  }

  @Test
  void seededUsersAreReadable() {
    User juan = userRepository.findByIdAndRoomId(1L, 1L).orElseThrow();

    assertThat(juan.getName()).isEqualTo("Juan P\u00e9rez");
    assertThat(juan.getRole()).isEqualTo(Role.ADMIN);
    assertThat(userRepository.findByIdAndRoomId(2L, 2L).orElseThrow().getRole()).isEqualTo(Role.SUPERADMIN);
  }

  @Test
  void savesAUserGeneratingItsId() {
    User saved = userRepository.save(newUser("pablo@email.com", "677998899", "23454234W"));

    assertThat(saved.getId()).isNotNull().isPositive();
    User found = userRepository.findByIdAndRoomId(saved.getId(), 1L).orElseThrow();
    assertThat(found.getName()).isEqualTo("pablo");
    assertThat(found.getEmail()).isEqualTo("pablo@email.com");
    assertThat(found.getPhone()).isEqualTo("677998899");
    assertThat(found.getRole()).isEqualTo(Role.ADMIN);
    assertThat(found.getDni()).isEqualTo("23454234W");
    assertThat(found.getRoomId()).isEqualTo(1L);
  }

  @Test
  void aUserIsOnlyFoundInItsRoom() {
    User saved = userRepository.save(newUser("pablo@email.com", "677998899", "23454234W"));

    assertThat(userRepository.findByIdAndRoomId(saved.getId(), 2L)).isEmpty();
    assertThat(userRepository.findByIdAndRoomId(999_999L, 1L)).isEmpty();
  }

  @Test
  void checksEmailExistence() {
    assertThat(userRepository.existsByEmail("juan@example.com")).isTrue();
    assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
  }

  @ParameterizedTest(name = "duplicated {0}")
  @CsvSource({
      "email, juan@example.com, 677998899, 23454234W",
      "dni,   pablo@email.com,  677998899, 11111111H",
      "phone, pablo@email.com,  600000001, 23454234W"})
  void uniqueViolationsAreTranslatedToTheViolatedField(String field, String email, String phone, String dni) {
    User duplicated = newUser(email, phone, dni);

    assertThatThrownBy(() -> userRepository.save(duplicated))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessage("error validation " + field);
  }

  private static User newUser(String email, String phone, String dni) {
    return User.create("pablo", email, phone, "admin", dni, 1L);
  }
}

