package devs.group5.rms.stepdefinitions;

import devs.group5.rms.controllers.AuthController;
import devs.group5.rms.dtos.LoginRequest;
import devs.group5.rms.dtos.SignUpRequest;
import devs.group5.rms.dtos.TokenResponse;
import devs.group5.rms.entities.User;
import devs.group5.rms.repositories.UserRepository;
import devs.group5.rms.services.JwtService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class LoginSteps {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private ResponseEntity<TokenResponse> response;
    private Exception caughtException;

    @Given("que existe un usuario con nombre {string} y contraseña {string}")
    public void que_existe_un_usuario_con_nombre_y_contrasena(String username, String password) {
        if (!userRepository.existsByName(username)) {
            val user = authController.registerAdmin(new SignUpRequest(username, password));
            this.user = userRepository.findById(user.id()).orElseThrow();
        }
    }

    @When("inicio sesión como {string} y con la contraseña {string}")
    public void inicio_sesion_como_y_con_la_contrasena(String username, String password) {
        try {
            response = authController.login(new LoginRequest(username, password));
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("obtengo un token que representa mi ID y mi rol en la sesión")
    public void obtengo_un_token_que_representa_mi_id_y_mi_rol_en_la_sesion() {
        assertNull(caughtException, "No debería haber lanzado una excepción");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        val body = response.getBody();
        assertNotNull(body);

        val token = body.access();
        val userId = jwtService.extractUserId(token);
        assertEquals(user.getId(), userId);

        val role = jwtService.extractUserRole(token);
        assertEquals(user.getRole(), role);
    }

    @Then("obtengo un error de credenciales inválidas")
    public void obtengo_un_error_de_credenciales_invalidas() {
        assertNotNull(caughtException, "Debería haber lanzado una excepción por credenciales inválidas");
    }

    @And("no obtengo un token de sesión")
    public void no_obtengo_un_token_de_sesion() {
        assertNull(response, "No debería haber obtenido una respuesta exitosa");
    }

    @Given("que no existe ningún usuario con nombre {string}")
    public void que_no_existe_ningun_usuario_con_nombre(String username) {
        userRepository.findByName(username).ifPresent(user -> userRepository.delete(user));
    }

    @Then("obtengo un error indicando que el usuario no existe")
    public void obtengo_un_error_indicando_que_el_usuario_no_existe() {
        assertNotNull(caughtException, "Debería haber lanzado una excepción indicando que no existe");
    }

    @When("intento iniciar sesión sin ingresar usuario ni contraseña")
    public void intento_iniciar_sesion_sin_ingresar_usuario_ni_contrasena() {
        try {
            response = authController.login(new LoginRequest(null, null));
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("el sistema debe rechazar la solicitud e indicar que los campos son obligatorios")
    public void el_sistema_debe_rechazar_la_solicitud_e_indicar_que_los_campos_son_obligatorios() {
        assertNotNull(caughtException, "Debería haber fallado al iniciar sesión sin credenciales");
    }
}
