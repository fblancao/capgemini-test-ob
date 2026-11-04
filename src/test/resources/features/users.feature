# language: es
Característica: Gestión de usuarios de una sala
  Como consumidor de la API
  Quiero guardar y consultar usuarios de la sala 1
  Para que cada usuario quede registrado en una única sala y sea notificado según su rol

  Escenario: Guardar un usuario admin devuelve su id y le notifica por email
    Dado un usuario con nombre "pablo", email "email@email.com", teléfono "677998899", rol "admin" y dni "23454234W"
    Cuando se guarda el usuario en la sala 1
    Entonces la respuesta tiene el código 201
    Y la respuesta contiene el id del usuario guardado
    Y el dni "23454234W" se ha validado contra la API externa
    Y se envía un email a "email@email.com" con el mensaje "usuario guardado"
    Y no se envía ningún SMS

  Escenario: Guardar un usuario superadmin le notifica por SMS
    Dado un usuario con nombre "ana", email "ana@email.com", teléfono "677998800", rol "superadmin" y dni "23454235W"
    Cuando se guarda el usuario en la sala 1
    Entonces la respuesta tiene el código 201
    Y la respuesta contiene el id del usuario guardado
    Y se envía un SMS al teléfono "677998800" con el mensaje "usuario guardado"
    Y no se envía ningún email

  Escenario: Consultar un usuario guardado en la sala 1
    Dado un usuario guardado en la sala 1 con nombre "pablo", email "email@email.com", teléfono "677998899", rol "admin" y dni "23454234W"
    Cuando se consulta el usuario guardado en la sala 1
    Entonces la respuesta tiene el código 200
    Y el usuario devuelto tiene nombre "pablo", email "email@email.com", teléfono "677998899", rol "admin" y dni "23454234W"

  Escenario: Un usuario sólo está en una sala
    Dado un usuario guardado en la sala 1 con nombre "pablo", email "email@email.com", teléfono "677998899", rol "admin" y dni "23454234W"
    Cuando se consulta el usuario guardado en la sala 2
    Entonces la respuesta tiene el código 404

  Escenario: Consultar un usuario inexistente
    Cuando se consulta el usuario con id 999999 en la sala 1
    Entonces la respuesta tiene el código 404
    Y el código de error del cuerpo es 404

  Esquema del escenario: Rechazar un usuario que no cumple las validaciones: <motivo>
    Dado un usuario con nombre "<nombre>", email "<email>", teléfono "677998899", rol "<rol>" y dni "<dni>"
    Cuando se guarda el usuario en la sala 1
    Entonces la respuesta tiene el código 409
    Y el código de error del cuerpo es 409
    Y el mensaje de error es "<mensaje>"
    Y no se envía ninguna notificación

    Ejemplos:
      | motivo                        | nombre  | email           | rol        | dni       | mensaje                   |
      | nombre de más de 6 caracteres | pablito | email@email.com | admin      | 23454234W | error validation userName |
      | email sin @                   | pablo   | emailemail.com  | admin      | 23454234W | error validation email    |
      | email sin punto               | pablo   | email@emailcom  | admin      | 23454234W | error validation email    |
      | rol no permitido              | pablo   | email@email.com | user       | 23454234W | error validation rol      |
      | dni rechazado por la API      | pablo   | email@email.com | superadmin | 99999999w | error validation dni      |

  Escenario: Rechazar un usuario cuyo email ya existe
    Dado un usuario guardado en la sala 1 con nombre "pablo", email "email@email.com", teléfono "677998899", rol "admin" y dni "23454234W"
    Y un usuario con nombre "ana", email "email@email.com", teléfono "677998800", rol "superadmin" y dni "23454235W"
    Cuando se guarda el usuario en la sala 1
    Entonces la respuesta tiene el código 409
    Y el mensaje de error es "error validation email"
    Y no se ha consultado la API de DNI
    Y no se envía ninguna notificación

  Escenario: Guardar un usuario en una sala inexistente
    Dado un usuario con nombre "pablo", email "email@email.com", teléfono "677998899", rol "admin" y dni "23454234W"
    Cuando se guarda el usuario en la sala 999
    Entonces la respuesta tiene el código 404

  Escenario: La API de DNI no está disponible
    Dado que la API de DNI no está disponible
    Y un usuario con nombre "pablo", email "email@email.com", teléfono "677998899", rol "admin" y dni "23454234W"
    Cuando se guarda el usuario en la sala 1
    Entonces la respuesta tiene el código 503
    Y no se envía ninguna notificación

  Escenario: Un fallo en la notificación no impide guardar el usuario
    Dado que la API de notificaciones no está disponible
    Y un usuario con nombre "pablo", email "email@email.com", teléfono "677998899", rol "admin" y dni "23454234W"
    Cuando se guarda el usuario en la sala 1
    Entonces la respuesta tiene el código 201
    Y la respuesta contiene el id del usuario guardado

