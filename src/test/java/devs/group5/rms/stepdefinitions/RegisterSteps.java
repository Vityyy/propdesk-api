package devs.group5.rms.stepdefinitions;

import devs.group5.rms.controllers.AuthController;
import devs.group5.rms.dtos.SignUpRequest;
import devs.group5.rms.dtos.TokenResponse;
import devs.group5.rms.entities.User;
import devs.group5.rms.repositories.AdminRepository;
import devs.group5.rms.repositories.UserRepository;
import devs.group5.rms.services.JwtService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegisterSteps {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    private User user;
    private ResponseEntity<TokenResponse> response;
    private Exception caughtException;

    @Given("que no existe un administrador con nombre {string}")
    public void queNoExisteUnAdministradorConNombre(String username) {
        userRepository.findByName(username).ifPresent(user -> userRepository.delete(user));
    }

    @When("un administrador se registra como {string} con la contraseña {string}")
    public void unAdministradorSeRegistraComoConLaContraseña(String username, String password) {
        try {
            val user = authController.registerAdmin(new SignUpRequest(username, password));
            this.user = userRepository.findById(user.id()).orElseThrow();
        } catch (Exception e) {
            this.caughtException = e;
        }
    }

    @Then("existe un administrador con nombre {string}")
    public void existeUnAdministradorConNombre(String username) {
        assertTrue(userRepository.existsByName(username));
    }

    @And("existe un administrador con contraseña {string}")
    public void existeUnAdministradorConContraseña(String password) {
        assertTrue(
                userRepository
                        .findAll()
                        .stream()
                        .map(User::getPassword)
                        .anyMatch(hashed -> passwordEncoder.matches(password, hashed))
        );
    }

    @Given("que no existe un dueño con nombre {string}")
    public void queNoExisteUnDueñoConNombre(String username) {
        userRepository.findByName(username).ifPresent(user -> userRepository.delete(user));
    }

    @When("un dueño se registra como {string} con la contraseña {string}")
    public void unDueñoSeRegistraComoConLaContraseña(String username, String password) {
        try {
            val user = authController.registerOwner(new SignUpRequest(username, password));
            this.user = userRepository.findById(user.id()).orElseThrow();
        } catch (Exception e) {
            this.caughtException = e;
        }
    }

    @Then("existe un dueño con nombre {string}")
    public void existeUnDueñoConNombre(String username) {
        assertTrue(userRepository.existsByName(username));
    }

    @And("existe un dueño con contraseña {string}")
    public void existeUnDuenoConContraseña(String password) {
        assertTrue(
                userRepository
                        .findAll()
                        .stream()
                        .map(User::getPassword)
                        .anyMatch(hashed -> passwordEncoder.matches(password, hashed))
        );
    }

    @Given("que existe un usuario con nombre {string}")
    public void queExisteUnUsuarioConNombre(String username) {
        if (!userRepository.existsByName(username)) {
            val user = authController.registerAdmin(new SignUpRequest(username, "123456789"));
            this.user = userRepository.findById(user.id()).orElseThrow();
        }
    }

    @When("un usuario se registra como {string} con la contraseña {string}")
    public void unUsuarioSeRegistraComoConLaContraseña(String username, String password) {
        try {
            val user = authController.registerAdmin(new SignUpRequest(username, password));
            this.user = userRepository.findById(user.id()).orElseThrow();
            adminRepository.flush();
        } catch (Exception e) {
            this.caughtException = e;
        }
    }

    @Then("se muestra un mensaje de error indicando que no pueden haber dos usuarios con el mismo nombre")
    public void seMuestraUnMensajeDeErrorIndicandoQueNoPuedenHaberDosUsuariosConElMismoNombre() {
        assertNotNull(this.caughtException);
    }

    @Given("que no existe un usuario con nombre {string}")
    public void queNoExisteUnUsuarioConNombre(String username) {
        userRepository.findByName(username).ifPresent(user -> userRepository.delete(user));
    }

    @Then("se muestra un mensaje de error indicando que es obligatorio llenar todos los campos")
    public void seMuestraUnMensajeDeErrorIndicandoQueEsObligatorioLlenarTodosLosCampos() {
        assertNotNull(this.caughtException);
    }
}
