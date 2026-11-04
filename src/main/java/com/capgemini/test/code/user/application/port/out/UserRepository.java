package com.capgemini.test.code.user.application.port.out;

import com.capgemini.test.code.user.domain.model.User;
import java.util.Optional;

public interface UserRepository {

  boolean existsByEmail(String email);

  /**
   * Persists a new user.
   *
   * @return the stored user, including its generated id
   * @throws com.capgemini.test.code.user.domain.exception.UserAlreadyExistsException if a unique attribute
   *     collides with an existing user
   */
  User save(User user);

  Optional<User> findByIdAndRoomId(Long id, Long roomId);
}

