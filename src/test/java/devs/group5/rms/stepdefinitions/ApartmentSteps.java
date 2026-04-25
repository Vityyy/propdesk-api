package devs.group5.rms.stepdefinitions;

import devs.group5.rms.controllers.ApartmentController;
import devs.group5.rms.dtos.ApartmentRequest;
import devs.group5.rms.dtos.ApartmentResponse;
import devs.group5.rms.entities.Property;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.PropertyRepository;
import devs.group5.rms.services.AuthService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ApartmentSteps {

    @Autowired
    private ApartmentController apartmentController;

    @Autowired
    private AuthService authService;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    private List<ApartmentResponse> response;
    private Exception caughtException;

    @Given("que existe un propietario con nombre {string} y contraseña {string}")
    public void que_existe_un_propietario_con_nombre_y_contrasena(String username, String password) {
        if (ownerRepository.findAll().stream().noneMatch(o -> o.getName().equals(username))) {
            authService.registerOwner(username, password);
        }
    }

    @Given("que existe una propiedad para el propietario {string} con nombre {string} y direccion {string}")
    public void que_existe_una_propiedad_para_el_propietario(String ownerName, String propName, String address) {
        var owner = ownerRepository.findAll().stream().filter(o -> o.getName().equals(ownerName)).findFirst().orElseThrow();
        // create property if not exists
        propertyRepository.findAll().stream()
                .filter(p -> p.getName().equals(propName) && p.getOwner().getId().equals(owner.getId()))
                .findFirst()
                .orElseGet(() -> propertyRepository.save(Property.builder().name(propName).address(address).owner(owner).build()));
    }

    @When("agrego un apartamento con número {int} y monto {int} para la propiedad {string} y propietario {string}")
    public void agrego_un_apartamento(int apartmentNumber, Integer amount, String propName, String ownerName) {
        try {
            var owner = ownerRepository.findAll().stream().filter(o -> o.getName().equals(ownerName)).findFirst().orElseThrow();
            var property = propertyRepository.findAll().stream().filter(p -> p.getName().equals(propName) && p.getOwner().getId().equals(owner.getId())).findFirst().orElseThrow();

            // Build a Jwt with subject = owner id
            // Jwt requires at least one header entry; provide a simple alg header for the test
            Map<String, Object> headers = Map.of("alg", "none");
            Map<String, Object> claims = Map.of("sub", owner.getId().toString());
            Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

            // Create an Authentication with ROLE_OWNER so the @PreAuthorize check in OwnerService passes
            var authToken = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_OWNER")));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            ApartmentRequest req = new ApartmentRequest(apartmentNumber, property.getId(), new BigDecimal(amount));
            response = apartmentController.addApartment(jwt, List.of(req));
            // clear context to avoid leaking auth between scenarios
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            e.printStackTrace();
            caughtException = e;
        }
    }

    @When("intento agregar un apartamento con número {int} y monto {int} para la propiedad {string} y propietario {string}")
    public void intento_agregar_un_apartamento(int apartmentNumber, Integer amount, String propName, String ownerName) {
        // reuse same implementation
        agrego_un_apartamento(apartmentNumber, amount, propName, ownerName);
    }

    @Then("obtengo una respuesta con {int} apartamento creado")
    public void obtengo_una_respuesta_con_apartamentos_creado(int expectedCount) {
        assertNull(caughtException, "No debería haber lanzado una excepción");
        assertNotNull(response);
        assertEquals(expectedCount, response.size());
    }

    @Then("el apartamento tiene número {int}")
    public void el_apartamento_tiene_nombre(int apartmentNumber) {
        assertNull(caughtException);
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(apartmentNumber, response.getFirst().number());
    }

    @Then("obtengo un error indicando que la propiedad no pertenece al propietario")
    public void obtengo_un_error_indicando_que_la_propiedad_no_pertenece_al_propietario() {
        assertNotNull(caughtException, "Debería haber lanzado una excepción por propiedad no perteneciente");
    }
}
