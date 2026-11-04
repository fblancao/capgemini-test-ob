package com.capgemini.test.code.user.application.port.in;

/**
 * Input data for {@link CreateUserUseCase}.
 */
public record CreateUserCommand(Long roomId, String name, String email, String phone, String role, String dni) {
}

