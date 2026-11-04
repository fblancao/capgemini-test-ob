package com.capgemini.test.code.user.infrastructure.rest;

import com.capgemini.test.code.user.domain.model.User;

public record UserResponse(Long id, String name, String email, String phone, String rol, String dni,
                           Long roomId) {

  static UserResponse from(User user) {
    return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
        user.getRole().getValue(), user.getDni(), user.getRoomId());
  }
}

