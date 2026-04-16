Feature: Inicio de Sesión de Usuario (US-12)
  Como usuario
  Quiero iniciar sesión
  Para acceder a mi cuenta

  Scenario: Inicio de sesión exitoso con credenciales correctas
    Given que existe un usuario con nombre "Usuario" y contraseña "123456789"
    When inicio sesión como "Usuario" y con la contraseña "123456789"
    Then obtengo un token que representa mi ID y mi rol en la sesión

  Scenario: Intento de inicio de sesión con contraseña incorrecta
    Given que existe un usuario con nombre "Usuario" y contraseña "123456789"
    When inicio sesión como "Usuario" y con la contraseña "ContraseñaIncorrecta"
    Then obtengo un error de credenciales inválidas
    And no obtengo un token de sesión

  Scenario: Intento de inicio de sesión con usuario inexistente
    Given que no existe ningún usuario con nombre "UsuarioFantasma"
    When inicio sesión como "UsuarioFantasma" y con la contraseña "123456789"
    Then obtengo un error indicando que el usuario no existe
    And no obtengo un token de sesión
    
  Scenario: Intento de inicio de sesión dejando campos en blanco
    When intento iniciar sesión sin ingresar usuario ni contraseña
    Then el sistema debe rechazar la solicitud e indicar que los campos son obligatorios
