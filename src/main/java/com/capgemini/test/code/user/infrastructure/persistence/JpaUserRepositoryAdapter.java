package com.capgemini.test.code.user.infrastructure.persistence;

import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.domain.exception.UserAlreadyExistsException;
import com.capgemini.test.code.user.domain.exception.UserValidationException;
import com.capgemini.test.code.user.domain.model.User;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
class JpaUserRepositoryAdapter implements UserRepository {

  private static final Map<String, String> UNIQUE_CONSTRAINTS = Map.of(
      "users_email_key", UserValidationException.EMAIL,
      "users_dni_key", UserValidationException.DNI,
      "users_phone_key", UserValidationException.PHONE);

  private final UserJpaRepository jpaRepository;

  JpaUserRepositoryAdapter(UserJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepository.existsByEmail(email);
  }

  /**
   * Unique constraints are the last line of defence against concurrent requests (the application checks
   * the e-mail beforehand): a violation is translated into the corresponding domain error.
   */
  @Override
  public User save(User user) {
    try {
      return UserEntityMapper.toDomain(jpaRepository.saveAndFlush(UserEntityMapper.toEntity(user)));
    } catch (DataIntegrityViolationException e) {
      throw violatedUniqueField(e)
          .<RuntimeException>map(UserAlreadyExistsException::new)
          .orElse(e);
    }
  }

  @Override
  public Optional<User> findByIdAndRoomId(Long id, Long roomId) {
    return jpaRepository.findByIdAndRoomId(id, roomId).map(UserEntityMapper::toDomain);
  }

  /**
   * Resolves the field from the PostgreSQL default unique constraint names ({@code <table>_<column>_key}).
   */
  private static Optional<String> violatedUniqueField(DataIntegrityViolationException e) {
    String message = String.valueOf(NestedExceptionUtils.getMostSpecificCause(e).getMessage())
        .toLowerCase(Locale.ROOT);
    return UNIQUE_CONSTRAINTS.entrySet().stream()
        .filter(entry -> message.contains(entry.getKey()))
        .map(Map.Entry::getValue)
        .findFirst();
  }
}





