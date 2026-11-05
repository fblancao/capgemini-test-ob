# Capgemini-Test

## Prerrequisitos

Asegúrese de tener instalados los siguientes componentes antes de ejecutar el proyecto:

- Java 17
- Maven
- Git
- Docker

---

## Ejecución del Docker para tener la infraestructura necesaria

1. Clone el repositorio:
   bash
   git clone <url-del-repositorio>
   
2. Navegue a la carpeta docker:
   bash
   cd docker
   
3. Levante los contenedores con Docker Compose:
   bash
   docker-compose up
   

### Contenedores del Docker

- *PostgreSQL:* Base de datos.
- *Mock-server:* API expuesta en el puerto 1080.

---

## APIs Expuestas en el Mock-server

### 1. API check-dni
- *Método:* PATCH
- *URL:* http://localhost:1080/check-dni
- *Body de la petición:*
  json
  {
    "dni": "<dni>"
  }
  
- *Respuestas:*
  - *OK:* Código HTTP 200 para cualquier DNI.
  - *KO:* Código HTTP 409 si el DNI es 99999999w.

---

### 2. API notification
#### Email
- *Método:* POST
- *URL:* http://localhost:1080/email
- *Body de la petición:*
  json
  {
    "email": "<email>",
    "message": "<msg>"
  }
  
- *Respuesta:*
  - *OK:* Código HTTP 200.

#### SMS
- *Método:* POST
- *URL:* http://localhost:1080/sms
- *Body de la petición:*
  json
  {
    "phone": "<phone>",
    "message": "<msg>"
  }
  
- *Respuesta:*
  - *OK:* Código HTTP 200.

---

## Contexto

La aplicación debe gestionar salas y usuarios bajo las siguientes condiciones:

- Cada sala tiene un ID único (long incremental).
- Una sala puede contener N usuarios.
- Un usuario puede estar en una única sala.
- Al guardar el usuario en la sala, se valida su DNI contra una API externa.
- Almacena el usuario en la base de datos y notifica según su rol.
- Devuelve el ID del usuario almacenado.

---

## Requisitos

### Validaciones:
1. *Nombre:* No debe contener más de 6 caracteres.
2. *Email:* Debe contener un @ y un ..
3. *Rol:* Sólo puede ser admin o superadmin.
4. Si el usuario ya existe (por email), se debe lanzar una excepción.
5. Validar el DNI contra la API externa del mock-server.

### Notificaciones:
- *Admin:* Notificación por email con el mensaje: "usuario guardado".
- *Superadmin:* Notificación por SMS con el mensaje: "usuario guardado".

### Respuesta esperada:
- *Retornar el ID generado.*

---

## Métodos a Implementar

### 1. Crear Usuario
- *Método:* POST
- *Descripción:* Guarda un usuario en la sala 1 y retorna su ID.
- *Ejemplo de JSON a guardar:*
  json
  {
    "name": "pablo",
    "email": "email@email.com",
    "phone": "677998899",
    "rol": "admin",
    "dni": "23454234W"
  }
  
- *Respuestas:*
  - *OK:* 
    - Código HTTP 201 Created
    - Body:
      json
      {
        "id": "<id>"
      }
      
  - *KO:* 
    - Código HTTP 409 Conflict
    - Body:
      json
      {
        "code": 409,
        "message": "error validation <email | userName | dni>"
      }
      

### 2. Obtener Usuario
- *Método:* GET
- *Descripción:* Obtiene un usuario basado en su ID dentro de la sala 1.

---

## Condiciones Opcionales

1. *Escalabilidad:* La aplicación podrá escalar para manejar diferentes contextos (pagos, pedidos, etc.).
2. *Capacidad:* La aplicación podrá procesar desde una hasta millones de peticiones (no es necesario implementar Kubernetes).

---

## Cosas que se Valoran

1. *Pruebas:*
   - Unitarias.
   - De integración.
   - De aceptación.
2. *Arquitectura e implementación.*
3. *Uso de Spring y abstracción del framework.*
4. *Patrones de diseño.*

---

## Método de Entrega

- Subir el proyecto a un repositorio público personal.
- Todos los commits deben estar realizados en la rama main.

---

# Solución

## Ejecución

```bash
# 1. Infraestructura (PostgreSQL + mock-server), tal y como indica el enunciado
cd docker
docker-compose up

# 2. Aplicación (puerto 8080)
mvn spring-boot:run

# Alternativa: infraestructura + aplicación en contenedores
docker-compose --profile app up --build
```

### Entorno local sin docker-compose ("Local env")

Si no hay Compose o no se puede descargar la imagen de mock-server, la aplicación se arranca con *servicios de
desarrollo* (Spring Boot 3.1+): `LocalCodeApplication` (en `src/test`) levanta PostgreSQL 17 con Testcontainers y
sirve las APIs externas con el stub WireMock en proceso (mismo contrato que `init.json`). Sólo hace falta un motor de
contenedores en marcha.

```powershell
podman machine start                      # o Docker en marcha
$env:TESTCONTAINERS_RYUK_DISABLED="true"  # sólo con Podman (bash: export TESTCONTAINERS_RYUK_DISABLED=true)
mvn spring-boot:test-run                  # Ctrl+C para parar: se elimina el contenedor
```

- La base de datos es efímera: cada arranque parte de las migraciones y el seed de Flyway.
- El stub escucha en `127.0.0.1:1080`, el mismo puerto que publica mock-server en docker-compose, así que las peticiones
  directas de la colección Postman (`check-dni`, `email`, `sms`) también funcionan. Requiere el puerto 1080 libre
  (no a la vez que el mock-server de docker-compose); se puede cambiar con `LOCAL_MOCKSERVER_PORT`.
- `mvn spring-boot:run` a secas **no** basta: necesita PostgreSQL en `localhost:5432` y mock-server en `localhost:1080`.

- Swagger UI: http://localhost:8080/swagger-ui.html (OpenAPI: `/v3/api-docs`)
- Health / probes: http://localhost:8080/actuator/health (`/liveness`, `/readiness`)
- Colección Postman actualizada en `postman/` (incluye las nuevas operaciones).
- Colección Bruno equivalente (verificada contra el entorno local) en `postman/capgemini.bruno_collection.yml`
  (Bruno → *Import collection*).

La configuración admite variables de entorno (12-factor): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_POOL_SIZE`,
`EXTERNAL_SERVICE_URL`.

## API

| Operación       | Método y ruta                      | OK                                  | KO                                                     |
|-----------------|------------------------------------|-------------------------------------|--------------------------------------------------------|
| Crear usuario   | `POST /rooms/1/users`              | `201 Created` + `{"id": 3}` + `Location` | `409` `{"code": 409, "message": "error validation <campo>"}` |
| Obtener usuario | `GET /rooms/1/users/{id}`          | `200 OK` + usuario                  | `404` `{"code": 404, "message": "..."}`                 |

La sala forma parte de la ruta (`/rooms/{roomId}/users`): el enunciado trabaja con la sala 1, pero el diseño admite N salas
sin cambios. Un usuario sólo se encuentra en su sala (consultarlo en otra devuelve `404`).

Errores (siempre con el cuerpo `{"code", "message"}` del enunciado):

| Caso                                                   | HTTP | `message`                               |
|--------------------------------------------------------|------|-----------------------------------------|
| Nombre vacío o de más de 6 caracteres                  | 409  | `error validation userName`             |
| Email sin `@` o sin `.` (o ya existente)               | 409  | `error validation email`                |
| Rol distinto de `admin` / `superadmin`                 | 409  | `error validation rol`                  |
| DNI rechazado por la API externa (409 del mock-server) | 409  | `error validation dni`                  |
| Teléfono vacío / DNI o teléfono duplicados             | 409  | `error validation phone` / `... dni`    |
| Sala o usuario inexistente                             | 404  | `room 9 not found` / `user 9 not found in room 1` |
| API de DNI no disponible (timeout, 5xx...)             | 503  | `external service unavailable: check-dni` |
| JSON mal formado / id no numérico                      | 400  | `malformed request`                     |

Orden de validación (de más barata a más cara): reglas de campos → existencia de la sala → email ya existente → API de DNI.
Tras guardar se notifica: **admin → email**, **superadmin → SMS**, con el mensaje `usuario guardado`. Un fallo de la
notificación se registra en el log pero no deshace el alta (el usuario ya está guardado).

## Arquitectura

Arquitectura hexagonal (puertos y adaptadores) organizada por *bounded context*, para que nuevos contextos (pagos, pedidos...)
se añadan como paquetes hermanos de `user` reutilizando `shared` y `clients`:

```
com.capgemini.test.code
├── clients/            Clientes Feign de las APIs externas (paquete propuesto por el enunciado; compartidos)
├── shared/
│   ├── domain/         Excepciones base por categoría (Validation→409, NotFound→404, ExternalService→503)
│   └── infrastructure/ Manejador global de errores REST, configuración de Feign
└── user/
    ├── domain/         Modelo (User, Role, NotificationChannel) y excepciones. Java puro, sin framework
    ├── application/    Casos de uso (CreateUserService, GetUserService, UserNotificationService)
    │   └── port/       in: CreateUserUseCase, GetUserUseCase · out: UserRepository, RoomRepository, DniValidator, NotificationSender
    └── infrastructure/ Adaptadores: REST, JPA, Feign (DNI y notificaciones) y el cableado Spring (UserConfiguration)
```

- **Abstracción del framework**: dominio y aplicación no dependen de Spring; se instancian en `UserConfiguration`.
- **Patrones**: Strategy (un `NotificationSender` por canal, elegido por el rol), Adapter (puertos de salida),
  Factory method (`User.create` valida / `User.restore` reconstruye), Command (`CreateUserCommand`),
  Singleton container (tests).
- **Persistencia**: Flyway. `V2__ids_as_bigint.sql` alinea los ids con el requisito *long incremental*.
  Las restricciones `UNIQUE` de BD son la última defensa ante peticiones concurrentes y se traducen al error del campo.

## Escalabilidad y capacidad (condiciones opcionales)

- Servicio *stateless* (sin sesión) → escalado horizontal detrás de un balanceador; imagen Docker sin root y con
  `MaxRAMPercentage`; probes de liveness/readiness para orquestadores.
- Pool de conexiones configurable (`DB_POOL_SIZE`), `open-in-view` desactivado, timeouts en los clientes HTTP.
- Siguientes pasos para "millones de peticiones": notificaciones asíncronas mediante *transactional outbox* + broker,
  circuit breaker/reintentos (Resilience4j) para las APIs externas y réplicas de lectura para las consultas.

## Tests

| Tipo        | Herramientas                                            | Ejecución                         |
|-------------|---------------------------------------------------------|-----------------------------------|
| Unitarios   | JUnit 5, Mockito, AssertJ, `@WebMvcTest`                | `mvn test` (sin infraestructura)  |
| Integración | Testcontainers (PostgreSQL 17), WireMock, OkHttp/Feign  | `mvn verify` (requiere Docker)    |
| Aceptación  | Cucumber (Gherkin en español), RestAssured              | `mvn verify` (requiere Docker)    |

- Los criterios de aceptación están en `src/test/resources/features/users.feature`.
- WireMock reproduce el contrato de `docker/mockserver-config/init.json`.
- Informes: `target/cucumber-report.html`, cobertura en `target/site/jacoco` y `target/site/jacoco-it`.
- CI: GitHub Actions (`.github/workflows/ci.yml`) ejecuta `mvn verify` en cada push / PR a `main`.
- Con Podman o redes que no permiten descargar la imagen de Ryuk: `TESTCONTAINERS_RYUK_DISABLED=true`.
- Testcontainers se fija a 1.21.4 (Boot 3.2 trae 1.19.x, cuyo cliente Docker usa la API 1.32, rechazada por
  Docker Engine 29+, p. ej. en los runners actuales de GitHub Actions).
- La configuración de Cucumber (glue, filtro `not @wip`, informe) está en `junit-platform.properties`, así que los
  escenarios se ejecutan igual desde Maven (`CucumberAcceptanceIT`) o desde el IDE (p. ej. `Tests - 1 Unit`).
- Codificación: todo el proyecto es UTF-8 (`.editorconfig`). Los literales Java con tildes (p. ej. los steps de Cucumber)
  usan escapes Unicode (`tel\u00e9fono`) para que los tests no dependan de la codificación del compilador del IDE.

### Configuraciones de IntelliJ IDEA

En `.run/` se versionan configuraciones de ejecución que IntelliJ carga automáticamente (desplegable *Run/Debug*):

| Configuración                           | Qué hace                                                       |
|-----------------------------------------|----------------------------------------------------------------|
| `Infra - docker-compose up`             | Levanta PostgreSQL + mock-server (`docker/docker-compose.yml`) |
| `App - CodeApplication`                 | Arranca la aplicación (puerto 8080); admite Debug              |
| `Maven - spring-boot:run`               | Arranca la aplicación con Maven                                |
| `App - Local env (Testcontainers)`      | "Local env": app + PostgreSQL (Testcontainers) + stub WireMock; admite Debug |
| `Maven - spring-boot:test-run (Local env)` | Lo mismo con Maven                                          |
| `Tests - 1 Unit`                        | Tests unitarios (`*Test`), sin infraestructura                 |
| `Tests - 2 Integration`                 | Tests de integración (`*IT`), requiere Docker/Podman           |
| `Tests - 3 Acceptance (Cucumber)`       | Escenarios Gherkin, requiere Docker/Podman                     |
| `Tests - 4 All (JUnit)`                 | Todos los tests desde el runner JUnit del IDE                  |
| `Maven - test (unit)` / `Maven - verify (all tests + coverage)` | Ciclo Maven completo, igual que la CI  |

## Cambios sobre el esqueleto proporcionado

| Fichero                    | Cambio y motivo                                                                                     |
|----------------------------|-----------------------------------------------------------------------------------------------------|
| `V1__init.sql`             | El seed insertaba usuarios sin `dni`/`phone` (`NOT NULL`) y con rol `USER`: la migración no podía ejecutarse nunca. |
| `DniClient`                | Usaba `feign.okhttp.OkHttpClient` sin la dependencia `feign-okhttp` (no compilaba). Se añade la dependencia (OkHttp es necesario para `PATCH`) y se elimina el `@Configuration` anidado (Spring Cloud OpenFeign desaconseja que la configuración de un cliente sea escaneada). |
| `CodeApplication`          | `@EnableFeignClients` pasa a `FeignConfiguration` para que los *test slices* no carguen Feign (guía "User Configuration and Slicing" de Spring Boot). |
| `pom.xml`                  | Spring Boot 3.2.0 → 3.2.12 y Spring Cloud 2023.0.0 → 2023.0.5 (parches de seguridad, misma línea). Flyway gestionado por Boot. |
| `application.yml`          | Variables de entorno, timeouts de Feign, `open-in-view: false`, `show-sql: false`, actuator.        |
| `docker-compose.yml`       | Versiones de imágenes fijadas (`latest` no es reproducible), healthcheck y servicio opcional `app`. |
| Postman                    | La petición `sms` apuntaba a `/check-dni`; se añaden las operaciones de la API.                       |

Nota: Flyway 9.22 (el gestionado por Spring Boot 3.2) muestra un aviso con PostgreSQL 17 (*support has not been tested*);
es sólo informativo y toda la batería de tests de integración se ejecuta contra PostgreSQL 17.

