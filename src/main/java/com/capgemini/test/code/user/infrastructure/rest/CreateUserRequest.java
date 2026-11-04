package com.capgemini.test.code.user.infrastructure.rest;

/**
 * Request body to create a user. Field names follow the specification (e.g. {@code rol}).
 */
public record CreateUserRequest(String name, String email, String phone, String rol, String dni) {
}

