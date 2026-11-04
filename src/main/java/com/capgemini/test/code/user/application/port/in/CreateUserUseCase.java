package com.capgemini.test.code.user.application.port.in;

/**
 * Stores a user in a room, validating it, and notifies it according to its role.
 */
public interface CreateUserUseCase {

  /**
   * @return the generated user id
   */
  Long create(CreateUserCommand command);
}

