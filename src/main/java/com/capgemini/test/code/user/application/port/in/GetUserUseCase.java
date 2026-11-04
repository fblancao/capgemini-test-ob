package com.capgemini.test.code.user.application.port.in;

import com.capgemini.test.code.user.domain.model.User;

/**
 * Retrieves a user that belongs to a room.
 */
public interface GetUserUseCase {

  User get(Long roomId, Long userId);
}

