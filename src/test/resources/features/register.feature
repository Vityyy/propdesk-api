Feature: Registro de Usuario (US-11)
  Como usuario
  Quiero registrarme en el sistema
  Para poder acceder a la aplicación

  Scenario: Registro exitoso con datos válidos (admin)
    Given que no existe un administrador con nombre "Admin"
    When un administrador se registra como "Admin" con la contraseña "123456789"
    Then existe un administrador con nombre "Admin"
    And existe un administrador con contraseña "123456789"

  Scenario: Registro exitoso con datos válidos (owner)
    Given que no existe un dueño con nombre "Owner"
    When un dueño se registra como "Owner" con la contraseña "123456789"
    Then existe un dueño con nombre "Owner"
    And existe un dueño con contraseña "123456789"

  Scenario: Intento de registro con un nombre ya existente
    Given que existe un usuario con nombre "Usuario"
    When un usuario se registra como "Usuario" con la contraseña "123456789"
    Then se muestra un mensaje de error indicando que no pueden haber dos usuarios con el mismo nombre

  Scenario: Intento de registro con campos obligatorios vacíos
    Given que no existe un usuario con nombre "Usuario"
    When un usuario se registra como "Usuario" con la contraseña ""
    Then se muestra un mensaje de error indicando que es obligatorio llenar todos los campos
