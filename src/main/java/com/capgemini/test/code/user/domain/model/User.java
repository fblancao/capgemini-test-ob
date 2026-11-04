package com.capgemini.test.code.user.domain.model;

import com.capgemini.test.code.user.domain.exception.UserValidationException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * User aggregate. A user always belongs to exactly one room (referenced by id).
 *
 * <p>Instances created through {@link #create} are guaranteed to satisfy the business rules; {@link #restore}
 * rebuilds an already persisted user without re-validating it (stored data is the source of truth).
 */
public final class User {

  public static final int NAME_MAX_LENGTH = 6;
  static final int EMAIL_MAX_LENGTH = 150;
  static final int PHONE_MAX_LENGTH = 15;
  static final int DNI_MAX_LENGTH = 15;

  /** Must contain an "@" and a "." (in the domain part). */
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  private final Long id;
  private final String name;
  private final String email;
  private final String phone;
  private final Role role;
  private final String dni;
  private final Long roomId;

  private User(Long id, String name, String email, String phone, Role role, String dni, Long roomId) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.role = role;
    this.dni = dni;
    this.roomId = roomId;
  }

  /**
   * Creates a new (not persisted yet) user applying every business validation.
   *
   * @throws UserValidationException naming the first invalid field
   */
  public static User create(String name, String email, String phone, String role, String dni, Long roomId) {
    Objects.requireNonNull(roomId, "roomId must not be null");
    return new User(
        null,
        validateName(name),
        validateEmail(email),
        validateRequired(phone, PHONE_MAX_LENGTH, UserValidationException.PHONE),
        Role.fromValue(role),
        validateRequired(dni, DNI_MAX_LENGTH, UserValidationException.DNI),
        roomId);
  }

  /**
   * Rebuilds a persisted user.
   */
  public static User restore(Long id, String name, String email, String phone, Role role, String dni,
      Long roomId) {
    return new User(Objects.requireNonNull(id, "id must not be null"), name, email, phone, role, dni, roomId);
  }

  private static String validateName(String name) {
    if (isBlank(name) || name.trim().length() > NAME_MAX_LENGTH) {
      throw new UserValidationException(UserValidationException.USER_NAME);
    }
    return name.trim();
  }

  private static String validateEmail(String email) {
    if (isBlank(email)) {
      throw new UserValidationException(UserValidationException.EMAIL);
    }
    String normalized = email.trim().toLowerCase(Locale.ROOT);
    if (normalized.length() > EMAIL_MAX_LENGTH || !EMAIL_PATTERN.matcher(normalized).matches()) {
      throw new UserValidationException(UserValidationException.EMAIL);
    }
    return normalized;
  }

  private static String validateRequired(String value, int maxLength, String field) {
    if (isBlank(value) || value.trim().length() > maxLength) {
      throw new UserValidationException(field);
    }
    return value.trim();
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public Role getRole() {
    return role;
  }

  public String getDni() {
    return dni;
  }

  public Long getRoomId() {
    return roomId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof User other)) {
      return false;
    }
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    // Personal data (email, phone, dni) intentionally omitted to avoid leaking it into logs
    return "User{id=" + id + ", role=" + role + ", roomId=" + roomId + '}';
  }
}

