package com.capgemini.test.code.acceptance;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.capgemini.test.code.support.ExternalServicesStub;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Step definitions (a new instance per scenario).
 *
 * <p>Spanish accents are written as Unicode escapes so the steps match the UTF-8 feature file whatever
 * encoding the compiler (Maven, IDE) is configured with.
 */
public class UserSteps {

  private static final String SEED_EMAILS = "('juan@example.com', 'maria@example.com')";

  @LocalServerPort
  private int port;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private Map<String, String> user;
  private Response response;
  private Long savedUserId;

  @Before
  public void cleanState() {
    ExternalServicesStub.reset();
    jdbcTemplate.update("DELETE FROM users WHERE email NOT IN " + SEED_EMAILS);
  }

  @Dado("que la API de DNI no est\u00e1 disponible")
  public void dniApiUnavailable() {
    ExternalServicesStub.dniServiceFailsWith(500);
  }

  @Dado("que la API de notificaciones no est\u00e1 disponible")
  public void notificationApiUnavailable() {
    ExternalServicesStub.notificationServiceFailsWith(500);
  }

  @Dado("un usuario con nombre {string}, email {string}, tel\u00e9fono {string}, rol {string} y dni {string}")
  public void aUser(String name, String email, String phone, String rol, String dni) {
    user = new LinkedHashMap<>();
    user.put("name", name);
    user.put("email", email);
    user.put("phone", phone);
    user.put("rol", rol);
    user.put("dni", dni);
  }

  @Dado("un usuario guardado en la sala {long} con nombre {string}, email {string}, tel\u00e9fono {string}, rol {string} y dni {string}")
  public void aSavedUser(long roomId, String name, String email, String phone, String rol, String dni) {
    aUser(name, email, phone, rol, dni);
    saveUserInRoom(roomId);
    assertThat(response.statusCode()).isEqualTo(201);
    savedUserId = response.jsonPath().getLong("id");
    // Only the interactions of the step under test must be verified afterwards
    ExternalServicesStub.server().resetRequests();
  }

  @Cuando("se guarda el usuario en la sala {long}")
  public void saveUserInRoom(long roomId) {
    response = given().port(port).contentType(ContentType.JSON).body(user)
        .when().post("/rooms/{roomId}/users", roomId);
  }

  @Cuando("se consulta el usuario guardado en la sala {long}")
  public void getSavedUser(long roomId) {
    getUser(savedUserId, roomId);
  }

  @Cuando("se consulta el usuario con id {long} en la sala {long}")
  public void getUser(long userId, long roomId) {
    response = given().port(port).when().get("/rooms/{roomId}/users/{userId}", roomId, userId);
  }

  @Entonces("la respuesta tiene el c\u00f3digo {int}")
  public void statusIs(int status) {
    assertThat(response.statusCode()).as(response.asString()).isEqualTo(status);
  }

  @Entonces("la respuesta contiene el id del usuario guardado")
  public void bodyContainsTheId() {
    Long id = response.jsonPath().getLong("id");
    assertThat(id).isNotNull().isPositive();
    assertThat(response.header("Location")).endsWith("/rooms/1/users/" + id);
    assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM users WHERE id = ? AND room_id = 1",
        Integer.class, id)).isEqualTo(1);
  }

  @Entonces("el c\u00f3digo de error del cuerpo es {int}")
  public void errorCodeIs(int code) {
    assertThat(response.jsonPath().getInt("code")).isEqualTo(code);
  }

  @Entonces("el mensaje de error es {string}")
  public void errorMessageIs(String message) {
    assertThat(response.jsonPath().getString("message")).isEqualTo(message);
  }

  @Entonces("el usuario devuelto tiene nombre {string}, email {string}, tel\u00e9fono {string}, rol {string} y dni {string}")
  public void returnedUserIs(String name, String email, String phone, String rol, String dni) {
    assertThat(response.jsonPath().getLong("id")).isEqualTo(savedUserId);
    assertThat(response.jsonPath().getString("name")).isEqualTo(name);
    assertThat(response.jsonPath().getString("email")).isEqualTo(email);
    assertThat(response.jsonPath().getString("phone")).isEqualTo(phone);
    assertThat(response.jsonPath().getString("rol")).isEqualTo(rol);
    assertThat(response.jsonPath().getString("dni")).isEqualTo(dni);
    assertThat(response.jsonPath().getLong("roomId")).isEqualTo(1L);
  }

  @Entonces("el dni {string} se ha validado contra la API externa")
  public void dniWasValidated(String dni) {
    ExternalServicesStub.server().verify(1, patchRequestedFor(urlEqualTo("/check-dni"))
        .withRequestBody(equalToJson("{\"dni\": \"" + dni + "\"}")));
  }

  @Entonces("no se ha consultado la API de DNI")
  public void dniWasNotValidated() {
    ExternalServicesStub.server().verify(0, patchRequestedFor(urlEqualTo("/check-dni")));
  }

  @Entonces("se env\u00eda un email a {string} con el mensaje {string}")
  public void emailSent(String email, String message) {
    ExternalServicesStub.server().verify(1, postRequestedFor(urlEqualTo("/email"))
        .withRequestBody(equalToJson("{\"email\": \"" + email + "\", \"message\": \"" + message + "\"}")));
  }

  @Entonces("se env\u00eda un SMS al tel\u00e9fono {string} con el mensaje {string}")
  public void smsSent(String phone, String message) {
    ExternalServicesStub.server().verify(1, postRequestedFor(urlEqualTo("/sms"))
        .withRequestBody(equalToJson("{\"phone\": \"" + phone + "\", \"message\": \"" + message + "\"}")));
  }

  @Entonces("no se env\u00eda ning\u00fan email")
  public void noEmailSent() {
    ExternalServicesStub.server().verify(0, postRequestedFor(urlEqualTo("/email")));
  }

  @Entonces("no se env\u00eda ning\u00fan SMS")
  public void noSmsSent() {
    ExternalServicesStub.server().verify(0, postRequestedFor(urlEqualTo("/sms")));
  }

  @Entonces("no se env\u00eda ninguna notificaci\u00f3n")
  public void noNotificationSent() {
    noEmailSent();
    noSmsSent();
  }
}

