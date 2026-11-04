package com.capgemini.test.code.user.application.service;

import com.capgemini.test.code.user.application.port.in.GetUserUseCase;
import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.domain.exception.UserNotFoundException;
import com.capgemini.test.code.user.domain.model.User;

public class GetUserService implements GetUserUseCase {

  private final UserRepository userRepository;

  public GetUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public User get(Long roomId, Long userId) {
    return userRepository.findByIdAndRoomId(userId, roomId)
        .orElseThrow(() -> new UserNotFoundException(userId, roomId));
  }
}

