package com.capgemini.test.code.user.domain.exception;

import com.capgemini.test.code.shared.domain.exception.NotFoundException;

public class RoomNotFoundException extends NotFoundException {

  public RoomNotFoundException(Long roomId) {
    super("room " + roomId + " not found");
  }
}

