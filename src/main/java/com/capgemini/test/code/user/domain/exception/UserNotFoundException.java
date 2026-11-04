package com.capgemini.test.code.user.domain.exception;

import com.capgemini.test.code.shared.domain.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {

  public UserNotFoundException(Long userId, Long roomId) {
    super("user " + userId + " not found in room " + roomId);
  }
}

