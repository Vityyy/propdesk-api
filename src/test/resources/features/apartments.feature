Feature: Administrar apartamentos

  Scenario: Agregar un apartamento con éxito
    Given que existe un propietario con nombre "owner1" y contraseña "pass1"
    And que existe una propiedad para el propietario "owner1" con nombre "Prop1" y direccion "Addr1"
    When intento agregar un apartamento con número 1 y monto 100 para la propiedad "Prop1" y propietario "owner1"
    Then obtengo una respuesta con 1 apartamento creado
    And el apartamento tiene número 1

  Scenario: Agregar apartamento a propiedad que no pertenece al propietario falla
    Given que existe un propietario con nombre "owner1" y contraseña "pass1"
    And que existe un propietario con nombre "other" y contraseña "pass2"
    And que existe una propiedad para el propietario "other" con nombre "PropOther" y direccion "AddrO"
    When intento agregar un apartamento con número 1 y monto 200 para la propiedad "PropOther" y propietario "owner1"
    Then obtengo un error indicando que la propiedad no pertenece al propietario
