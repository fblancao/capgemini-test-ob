package com.capgemini.test.code.shared.infrastructure.rest;

/**
 * Error body defined by the specification: {@code {"code": 409, "message": "error validation email"}}.
 */
public record ErrorResponse(int code, String message) {
}

