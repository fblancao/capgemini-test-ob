package com.capgemini.test.code.user.infrastructure.persistence;

import com.capgemini.test.code.user.domain.model.User;

/**
 * Maps between the domain model and the JPA entity, keeping the domain free of persistence concerns.
 */
final class UserEntityMapper {

  private UserEntityMapper() {
  }

  static UserEntity toEntity(User user) {
    return new UserEntity(user.getId(), user.getName(), user.getEmail(), user.getDni(), user.getPhone(),
        user.getRole(), user.getRoomId());
  }

  static User toDomain(UserEntity entity) {
    return User.restore(entity.getId(), entity.getName(), entity.getEmail(), entity.getPhone(), entity.getRole(),
        entity.getDni(), entity.getRoomId());
  }
}

