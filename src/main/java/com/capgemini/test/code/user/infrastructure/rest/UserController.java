package com.capgemini.test.code.user.infrastructure.rest;

import com.capgemini.test.code.user.application.port.in.CreateUserCommand;
import com.capgemini.test.code.user.application.port.in.CreateUserUseCase;
import com.capgemini.test.code.user.application.port.in.GetUserUseCase;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Users of a room. The specification works with room 1: {@code POST /rooms/1/users} and
 * {@code GET /rooms/1/users/{id}}.
 */
@RestController
@RequestMapping("/rooms/{roomId}/users")
public class UserController {

  private final CreateUserUseCase createUserUseCase;
  private final GetUserUseCase getUserUseCase;

  public UserController(CreateUserUseCase createUserUseCase, GetUserUseCase getUserUseCase) {
    this.createUserUseCase = createUserUseCase;
    this.getUserUseCase = getUserUseCase;
  }

  @PostMapping
  public ResponseEntity<CreateUserResponse> create(@PathVariable Long roomId,
      @RequestBody CreateUserRequest request) {
    Long id = createUserUseCase.create(new CreateUserCommand(roomId, request.name(), request.email(),
        request.phone(), request.rol(), request.dni()));
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(new CreateUserResponse(id));
  }

  @GetMapping("/{userId}")
  public UserResponse get(@PathVariable Long roomId, @PathVariable Long userId) {
    return UserResponse.from(getUserUseCase.get(roomId, userId));
  }
}

